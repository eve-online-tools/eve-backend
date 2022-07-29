package de.ronnywalter.eve.config;

import org.springframework.context.annotation.Configuration;

@Configuration
//@EnableCaching
public class RedisConfig {

/*    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(60))
                .disableCachingNullValues();
//                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }
*/
  /*  @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        return (builder) -> builder
                .withCacheConfiguration("types",
                    RedisCacheConfiguration
                            .defaultCacheConfig()
                            .entryTtl(Duration.ofDays(1))
                            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())))
                .withCacheConfiguration("systems",
                    RedisCacheConfiguration
                            .defaultCacheConfig()
                            .entryTtl(Duration.ofDays(1))
                            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())))
                .withCacheConfiguration("locations",
                    RedisCacheConfiguration
                            .defaultCacheConfig()
                            .entryTtl(Duration.ofDays(1))
                            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())))
                .withCacheConfiguration("marketgroups",
                RedisCacheConfiguration
                        .defaultCacheConfig()
                        .entryTtl(Duration.ofDays(1))
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())));


    }

   */
}
