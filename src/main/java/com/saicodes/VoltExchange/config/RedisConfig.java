package com.saicodes.VoltExchange.config;

import com.saicodes.VoltExchange.entities.Wallet;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.*;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
   public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
       RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
               .entryTtl(Duration.ofMinutes(15))
               .disableCachingNullValues()
               .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new JacksonJsonRedisSerializer<>(Wallet.class)));
       return RedisCacheManager.builder(connectionFactory).cacheDefaults(config).build();
   }
}
