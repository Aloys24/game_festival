package com.miniw.fesweb;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 *
 * @author luoquan
 * @date 2021/08/19
 */
@SpringBootApplication(scanBasePackages = "com.miniw.*")
@EnableFeignClients(basePackages = "com.miniw.fesexternal.client")
@MapperScan(basePackages = "com.miniw.fespersistence.mapper")
@EnableScheduling
@EnableAsync
public class FesWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(FesWebApplication.class, args);
    }

}
