package com.redis.splunk.forwarder.configuration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import redis.clients.jedis.*;
import redis.clients.jedis.providers.PooledConnectionProvider;

@Component
public class JedisConfig {
    @Value("${redis.host}")
    String host = "localhost";
    @Value("${redis.port}")
    Integer port = 6379;

    private final static Logger logger = LogManager.getLogger(JedisConfig.class);

    @Bean
    public UnifiedJedis getClient() {
        HostAndPort config = new HostAndPort(host, port);
        PooledConnectionProvider provider = new PooledConnectionProvider(config);
        UnifiedJedis client = new UnifiedJedis(provider);
        logger.info("host {} - port {}", config.getHost(), config.getPort());
        return client;
    }

}