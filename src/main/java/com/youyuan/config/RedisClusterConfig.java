package com.youyuan.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * 类名称：RedisClusterConfig <br>
 * 类描述： redis集群自动配置 <br>
 *
 * @author zhangyu
 * @version 1.0.0
 * @date 创建时间：2021/8/21 12:29<br>
 */
@Component
@Data
@Configuration
public class RedisClusterConfig {

    @Value("${spring.redis.cluster.nodes}")
    private String nodes;
    @Value("${spring.redis.timeout}")
    private int timeout;
    @Value("${spring.redis.cluster.lettuce.pool.max-idle}")
    private int maxIdle;
    @Value("${spring.redis.cluster.lettuce.pool.max-wait}")
    private long maxWaitMillis;
    @Value("${spring.redis.cluster.command-timeout}")
    private int commandTimeout;
}
