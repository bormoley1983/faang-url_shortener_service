package faang.school.urlshortenerservice.config.redis;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
@Configuration
public class RedisConfig {
    private final RedisProperties redisProperties;

    @Bean(name = "jedisConnection")
    public JedisConnectionFactory jedisConnection() {
        RedisStandaloneConfiguration connection = new RedisStandaloneConfiguration(redisProperties.getHost(),
                redisProperties.getPort());
        if (StringUtils.hasText(redisProperties.getPassword())) {
            connection.setPassword(redisProperties.getPassword());
        }
        return new JedisConnectionFactory(connection);
    }

    @Bean(name = "redisTemplateHash")
    public RedisTemplate<String, Object> redisTemplateHash(@Qualifier(value = "jedisConnection")
                                                           JedisConnectionFactory jedisConnection) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        Jackson2JsonRedisSerializer<Object> jackson = new Jackson2JsonRedisSerializer<>(Object.class);
        template.setConnectionFactory(jedisConnection);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(jackson);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(jackson);
        template.afterPropertiesSet();
        return template;
    }
}