package com.ziwei.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * WeChat Mini Program configuration properties
 *
 * @author JTWORLD
 */
@Data
@Component
@ConfigurationProperties(prefix = "wechat")
public class WechatProperties {

    /** WeChat Mini Program AppID */
    private String appId;

    /** WeChat Mini Program AppSecret */
    private String appSecret;

}
