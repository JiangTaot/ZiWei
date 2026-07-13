package com.ziwei;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 紫微斗数排盘服务启动类
 *
 * @author JTWORLD
 */
@SpringBootApplication
@MapperScan("com.ziwei.mapper")
public class ZiWeiServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZiWeiServerApplication.class, args);
    }

}
