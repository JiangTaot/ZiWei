package com.ziwei.service;

import com.ziwei.dto.ZiweiChartVO;
import com.ziwei.dto.ZiweiPalaceVO;
import com.ziwei.dto.ZiweiStarVO;
import com.ziwei.enums.ErrorCodeConstants;
import com.ziwei.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Ziwei AI chart interpretation service implementation
 * <p>
 * Uses DeepSeek AI via Spring AI for professional Ziwei DouShu readings,
 * based on the SanHe (三合) school methodology from Ni Hai-Xia's "Tian Ji" system.
 *
 * @author JTWORLD
 */
@Service
@Slf4j
public class ZiweiAiInterpretServiceImpl implements ZiweiAiInterpretService {

    @Resource
    private ZiweiChartService chartService;

    @Resource
    private ChatModel chatModel;

    @Override
    public String interpretChart(Long chartId, String userQuestion) {
        ZiweiChartVO chart = chartService.getChartById(chartId);
        if (chart == null) {
            throw new BusinessException(ErrorCodeConstants.CHART_NOT_EXISTS);
        }

        String chartData = buildChartDataText(chart);
        String question = (userQuestion != null && !userQuestion.isBlank())
                ? userQuestion : "请对此命盘进行全面解读分析。";

        Prompt prompt = new Prompt(List.of(
                new SystemMessage(buildSystemPrompt()),
                new UserMessage("以下是命盘数据：\n\n" + chartData + "\n\n用户问题：" + question)
        ));

        try {
            return chatModel.call(prompt).getResult().getOutput().getText();
        } catch (Exception e) {
            log.error("AI interpretation failed for chartId={}", chartId, e);
            throw new BusinessException(ErrorCodeConstants.AI_INTERPRET_ERROR);
        }
    }

    @Override
    public Flux<String> interpretChartStream(Long chartId, String userQuestion) {
        ZiweiChartVO chart = chartService.getChartById(chartId);
        if (chart == null) {
            throw new BusinessException(ErrorCodeConstants.CHART_NOT_EXISTS);
        }

        String chartData = buildChartDataText(chart);
        String question = (userQuestion != null && !userQuestion.isBlank())
                ? userQuestion : "请对此命盘进行全面解读分析。";

        Prompt prompt = new Prompt(List.of(
                new SystemMessage(buildSystemPrompt()),
                new UserMessage("以下是命盘数据：\n\n" + chartData + "\n\n用户问题：" + question)
        ));

        try {
            return chatModel.stream(prompt).map(response -> {
                String text = response.getResult() != null && response.getResult().getOutput() != null
                        ? response.getResult().getOutput().getText() : "";
                return text != null ? text : "";
            });
        } catch (Exception e) {
            log.error("AI streaming interpretation failed for chartId={}", chartId, e);
            throw new BusinessException(ErrorCodeConstants.AI_INTERPRET_ERROR);
        }
    }

    @Override
    public String interpretPalace(Long chartId, int palaceType, String userQuestion) {
        ZiweiChartVO chart = chartService.getChartById(chartId);
        if (chart == null) {
            throw new BusinessException(ErrorCodeConstants.CHART_NOT_EXISTS);
        }

        // Find target palace
        ZiweiPalaceVO targetPalace = null;
        if (chart.getPalaces() != null) {
            for (ZiweiPalaceVO p : chart.getPalaces()) {
                if (p.getPalaceType() == palaceType) {
                    targetPalace = p;
                    break;
                }
            }
        }
        if (targetPalace == null) {
            throw new BusinessException(ErrorCodeConstants.CHART_NOT_EXISTS);
        }

        String palaceData = buildPalaceDataText(chart, targetPalace);
        String question = (userQuestion != null && !userQuestion.isBlank())
                ? userQuestion : "请解读此宫位。";

        Prompt prompt = new Prompt(List.of(
                new SystemMessage(buildPalaceSystemPrompt()),
                new UserMessage("以下是命盘与宫位数据：\n\n" + palaceData + "\n\n用户问题：" + question)
        ));

        try {
            return chatModel.call(prompt).getResult().getOutput().getText();
        } catch (Exception e) {
            log.error("AI palace interpretation failed for chartId={}, palaceType={}", chartId, palaceType, e);
            throw new BusinessException(ErrorCodeConstants.AI_INTERPRET_ERROR);
        }
    }

    // ========== Chart Data Formatting ==========

    /**
     * Build structured chart data text for AI prompt
     */
    private String buildChartDataText(ZiweiChartVO chart) {
        StringBuilder sb = new StringBuilder();

        // Basic info
        sb.append("【基本信息】\n");
        sb.append("八字：").append(chart.getYearPillar()).append("年 ")
                .append(chart.getMonthPillar()).append("月 ")
                .append(chart.getDayPillar()).append("日 ")
                .append(chart.getHourPillar()).append("时\n");
        sb.append("命宫：").append(chart.getMingGong()).append("\n");
        sb.append("身宫：").append(chart.getShenGong()).append("\n");
        sb.append("五行局：").append(chart.getWuxingJu()).append("\n");
        sb.append("性别：").append(chart.getGender() == 1 ? "男" : "女").append("\n\n");

        // Twelve palaces
        sb.append("【十二宫配置】\n");
        if (chart.getPalaces() != null) {
            for (ZiweiPalaceVO palace : chart.getPalaces()) {
                sb.append(palace.getPalaceName()).append("（")
                        .append(palace.getDizhi()).append("宫，天干")
                        .append(palace.getTianGan()).append("）");
                if (palace.isShenGong()) {
                    sb.append("【身宫】");
                }
                sb.append("：\n");

                // Major stars
                if (palace.getMajorStars() != null && !palace.getMajorStars().isEmpty()) {
                    sb.append("  主星：");
                    sb.append(palace.getMajorStars().stream()
                            .map(this::formatStar)
                            .collect(Collectors.joining("、")));
                    sb.append("\n");
                }

                // Auxiliary stars
                if (palace.getAuxiliaryStars() != null && !palace.getAuxiliaryStars().isEmpty()) {
                    sb.append("  辅星：");
                    sb.append(palace.getAuxiliaryStars().stream()
                            .map(this::formatStar)
                            .collect(Collectors.joining("、")));
                    sb.append("\n");
                }

                // Minor stars
                if (palace.getMinorStars() != null && !palace.getMinorStars().isEmpty()) {
                    sb.append("  杂曜：");
                    sb.append(palace.getMinorStars().stream()
                            .map(ZiweiStarVO::getStarName)
                            .collect(Collectors.joining("、")));
                    sb.append("\n");
                }
            }
        }

        // Natal Sihua
        sb.append("\n【本命四化】\n");
        if (chart.getNatalSihua() != null) {
            ZiweiChartVO.NatalSihuaVO sihua = chart.getNatalSihua();
            appendSihuaStar(sb, "化禄", sihua.getHuaLu());
            appendSihuaStar(sb, "化权", sihua.getHuaQuan());
            appendSihuaStar(sb, "化科", sihua.getHuaKe());
            appendSihuaStar(sb, "化忌", sihua.getHuaJi());
        }

        // Daxian
        sb.append("\n【大限运行】\n");
        if (chart.getDaxians() != null) {
            for (ZiweiChartVO.DaxianVO dx : chart.getDaxians()) {
                sb.append(dx.getAgeStart()).append("-").append(dx.getAgeEnd()).append("岁：")
                        .append(dx.getPalaceName()).append("（").append(dx.getDirection()).append("）\n");
            }
        }

        // Patterns
        sb.append("\n【命格格局】\n");
        if (chart.getPatterns() != null && !chart.getPatterns().isEmpty()) {
            sb.append(String.join("、", chart.getPatterns())).append("\n");
        } else {
            sb.append("无特殊格局\n");
        }

        return sb.toString();
    }

    /**
     * Build palace-specific data text for AI prompt
     */
    private String buildPalaceDataText(ZiweiChartVO chart, ZiweiPalaceVO palace) {
        StringBuilder sb = new StringBuilder();

        sb.append("命盘八字：").append(chart.getYearPillar()).append(" ")
                .append(chart.getMonthPillar()).append(" ")
                .append(chart.getDayPillar()).append(" ")
                .append(chart.getHourPillar()).append("\n");
        sb.append("目标宫位：").append(palace.getPalaceName()).append("（")
                .append(palace.getDizhi()).append("宫，天干").append(palace.getTianGan()).append("）\n");

        if (palace.getMajorStars() != null && !palace.getMajorStars().isEmpty()) {
            sb.append("主星：");
            sb.append(palace.getMajorStars().stream()
                    .map(this::formatStar)
                    .collect(Collectors.joining("、")));
            sb.append("\n");
        }
        if (palace.getAuxiliaryStars() != null && !palace.getAuxiliaryStars().isEmpty()) {
            sb.append("辅星：");
            sb.append(palace.getAuxiliaryStars().stream()
                    .map(this::formatStar)
                    .collect(Collectors.joining("、")));
            sb.append("\n");
        }
        if (palace.getMinorStars() != null && !palace.getMinorStars().isEmpty()) {
            sb.append("杂曜：");
            sb.append(palace.getMinorStars().stream()
                    .map(ZiweiStarVO::getStarName)
                    .collect(Collectors.joining("、")));
            sb.append("\n");
        }

        // Opposite palace (SanFang SiZheng)
        int oppositeType = palace.getPalaceType() <= 6 ? palace.getPalaceType() + 6 : palace.getPalaceType() - 6;
        if (chart.getPalaces() != null) {
            for (ZiweiPalaceVO p : chart.getPalaces()) {
                if (p.getPalaceType() == oppositeType) {
                    sb.append("对宫（").append(p.getPalaceName()).append("）：");
                    if (p.getMajorStars() != null && !p.getMajorStars().isEmpty()) {
                        sb.append(p.getMajorStars().stream()
                                .map(this::formatStar)
                                .collect(Collectors.joining("、")));
                    } else {
                        sb.append("无主星");
                    }
                    sb.append("\n");
                    break;
                }
            }
        }

        return sb.toString();
    }

    /**
     * Format a star with brightness and sihua for prompt
     */
    private String formatStar(ZiweiStarVO star) {
        StringBuilder sb = new StringBuilder(star.getStarName());
        if (star.getBrightness() != null && !star.getBrightness().isEmpty()) {
            sb.append("[").append(star.getBrightness()).append("]");
        }
        if (star.getSihuaType() != null && !star.getSihuaType().isEmpty()) {
            sb.append("(").append(star.getSihuaType()).append(")");
        }
        return sb.toString();
    }

    private void appendSihuaStar(StringBuilder sb, String label, ZiweiStarVO star) {
        if (star != null) {
            sb.append(label).append("：").append(star.getStarName());
            if (star.getSihuaType() != null && !star.getSihuaType().isEmpty()) {
                sb.append("(").append(star.getSihuaType()).append(")");
            }
            sb.append("\n");
        }
    }

    // ========== System Prompts ==========

    /**
     * System prompt for full chart interpretation
     */
    private String buildSystemPrompt() {
        return """
                你是一位精通紫微斗数的命理专家，擅长三合派分析法（倪海夏《天纪》体系）。
                请根据用户提供的命盘数据进行专业、客观、有条理的解读。

                解读要求：
                1. 首先概述命盘的整体特点（命宫主星、五行局、格局等）
                2. 分析命宫及其三方四正的星曜配置，解读性格特质和人生主题
                3. 分析关键宫位：财帛宫（财运）、官禄宫（事业）、夫妻宫（婚姻感情）
                4. 解读本命四化（化禄、化权、化科、化忌）对人生的影响
                5. 分析大限走势，指出重要的人生阶段
                6. 如果命盘中有特殊格局，请说明其含义和影响
                7. 最后给出综合建议，帮助命主趋吉避凶

                注意事项：
                - 请使用通俗易懂的中文，保持东方雅致文风
                - 避免过于晦涩的术语，必要时加注解释
                - 请保持客观中立，避免绝对化的断言
                - 结尾请加上免责声明：以上解读仅供参考，命运掌握在自己手中
                """;
    }

    /**
     * System prompt for single palace interpretation
     */
    private String buildPalaceSystemPrompt() {
        return """
                你是一位精通紫微斗数的命理专家，擅长三合派分析法。
                请根据用户提供的命盘和宫位数据，对该宫位进行专业解读。

                解读要求：
                1. 分析该宫位的主星配置及其含义
                2. 分析辅星、杂曜的加持或削弱作用
                3. 说明星曜亮度（庙旺利陷）对该宫位的影响
                4. 如果有四化星落入该宫，说明其影响
                5. 结合对宫星曜进行综合分析（三方四正）
                6. 给出该宫位相关的具体建议

                请使用通俗易懂的中文，保持东方雅致文风。
                结尾请加上免责声明。
                """;
    }

}
