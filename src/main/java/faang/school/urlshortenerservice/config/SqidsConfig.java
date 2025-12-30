package faang.school.urlshortenerservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.sqids.Sqids;

@Configuration
public class SqidsConfig {

    @Bean
    public Sqids sqids() {
        return new Sqids.Builder()
                .alphabet("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789")
                .minLength(6)
                .build();
    }
}
