package faang.school.urlshortenerservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@RedisHash(value = "urls", timeToLive = 60 * 60 * 24 * 30)
public class UrlRedis {

    @Id
    private String hash;

    private String longLing;
    private LocalDateTime createdAt;

}
