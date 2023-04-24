package com.miniw.fescommon.config;

import org.redisson.Redisson;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * redis配置类
 *
 * @author luoquan
 * @date 2021/08/23
 */
@Component
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.database}")
    private Integer database;
    @Value("${spring.redis.port}")
    private Integer port;
    @Value("${spring.redis.password}")
    private String password;


    @Bean
    public Redisson redisson() {
        Config config = new Config();
        SingleServerConfig singleServerConfig = config.useSingleServer();
        singleServerConfig.setAddress("redis://" + host + ":" + port);
        singleServerConfig.setPassword(password);
        singleServerConfig.setDatabase(database);
        singleServerConfig.setConnectionPoolSize(16);
        singleServerConfig.setConnectionMinimumIdleSize(2);
        return (Redisson) Redisson.create(config);
    }
}
