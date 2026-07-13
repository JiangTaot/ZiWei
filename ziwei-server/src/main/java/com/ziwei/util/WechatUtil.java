package com.ziwei.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ziwei.config.WechatProperties;
import com.ziwei.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * WeChat Mini Program API utility
 *
 * @author JTWORLD
 */
@Slf4j
@Component
public class WechatUtil {

    private static final String CODE2SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session";

    @Resource
    private WechatProperties wechatProperties;

    /**
     * Exchange login code for session info via WeChat API
     *
     * @param code wx.login() return value
     * @return map containing openid, session_key, unionid (optional)
     */
    public Map<String, String> code2Session(String code) {
        Map<String, Object> params = new HashMap<>();
        params.put("appid", wechatProperties.getAppId());
        params.put("secret", wechatProperties.getAppSecret());
        params.put("js_code", code);
        params.put("grant_type", "authorization_code");

        String response = HttpUtil.get(CODE2SESSION_URL, params);
        log.debug("WeChat code2Session response: {}", response);

        JSONObject json = JSONUtil.parseObj(response);

        // Check for WeChat API error
        if (json.containsKey("errcode") && json.getInt("errcode") != 0) {
            String errMsg = json.getStr("errmsg", "unknown error");
            log.error("WeChat code2Session failed: errcode={}, errmsg={}", json.getInt("errcode"), errMsg);
            throw new BusinessException(401, "WeChat login failed: " + errMsg);
        }

        String openid = json.getStr("openid");
        if (StrUtil.isBlank(openid)) {
            throw new BusinessException(401, "WeChat login failed: empty openid");
        }

        Map<String, String> result = new HashMap<>();
        result.put("openid", openid);
        result.put("session_key", json.getStr("session_key", ""));
        result.put("unionid", json.getStr("unionid", ""));
        return result;
    }

}
