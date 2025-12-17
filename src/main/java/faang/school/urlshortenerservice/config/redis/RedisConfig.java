package faang.school.urlshortenerservice.config.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        return new JedisConnectionFactory(new RedisStandaloneConfiguration(redisHost, redisPort));
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(JedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        objectMapper.findAndRegisterModules();

        Jackson2JsonRedisSerializer<Object> jackson =
                new Jackson2JsonRedisSerializer<>(Object.class);
        jackson.setObjectMapper(objectMapper);

        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(jackson);
        template.setHashValueSerializer(jackson);

        return template;
    }
}
