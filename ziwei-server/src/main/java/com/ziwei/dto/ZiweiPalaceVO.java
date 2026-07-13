package com.ziwei.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Palace view object (embedded in ChartVO)
 *
 * @author JTWORLD
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZiweiPalaceVO {

    /** Palace type code (1-12) */
    private int palaceType;

    /** Palace name in Chinese */
    private String palaceName;

    /** DiZhi (地支) name */
    private String dizhi;

    /** TianGan (天干) name */
    private String tianGan;

    /** Whether this palace is the body palace (身宫) */
    private boolean isShenGong;

    /** DaXian age range label (e.g. "3-12岁") */
    private String daXianLabel;

    /** Major stars in this palace */
    @Builder.Default
    private List<ZiweiStarVO> majorStars = new ArrayList<>();

    /** Auxiliary stars in this palace */
    @Builder.Default
    private List<ZiweiStarVO> auxiliaryStars = new ArrayList<>();

    /** Minor stars in this palace */
    @Builder.Default
    private List<ZiweiStarVO> minorStars = new ArrayList<>();

}
