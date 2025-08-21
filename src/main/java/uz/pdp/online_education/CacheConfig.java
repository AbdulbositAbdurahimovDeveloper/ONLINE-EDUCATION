package uz.pdp.online_education;

import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import uz.pdp.online_education.enums.CacheType;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class CacheConfig {

    /**
     * Creates and configures the primary RedisCacheManager for the application.
     * This manager uses Redis as the caching backend and dynamically builds a set of
     * pre-configured caches based on the CacheType enum, each with its own specific TTL.
     *
     * @param connectionFactory Auto-injected by Spring, provides connection to Redis.
     * @return The configured RedisCacheManager.
     */
    @Bean
    @Primary
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        Map<String, RedisCacheConfiguration> initialCacheConfigurations = new HashMap<>();

        for (CacheType cacheType : CacheType.values()) {
            initialCacheConfigurations.put(
                    cacheType.getCacheName(),
                    createCacheConfiguration(cacheType.getTtlSeconds())
            );
        }

        return RedisCacheManager.builder(connectionFactory)
                .withInitialCacheConfigurations(initialCacheConfigurations)
                .build();
    }

    /**
     * A helper method to create a RedisCacheConfiguration with a specific TTL.
     * It also defines how keys (as String) and values (as JSON) are serialized.
     *
     * @param ttlSeconds Time-to-live for the cache entries in seconds.
     * @return A configured RedisCacheConfiguration instance.
     */
    private RedisCacheConfiguration createCacheConfiguration(long ttlSeconds) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(ttlSeconds)) // Kesh uchun TTL'ni belgilaymiz
                .disableCachingNullValues() // null qiymatlarni keshlamaslik (tavsiya etiladi)
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(new StringRedisSerializer());

        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        return template;
    }
}