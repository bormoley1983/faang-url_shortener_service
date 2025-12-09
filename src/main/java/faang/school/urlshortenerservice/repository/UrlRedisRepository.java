package faang.school.urlshortenerservice.repository;

import faang.school.urlshortenerservice.entity.UrlRedis;
import org.springframework.data.repository.CrudRepository;

public interface UrlRedisRepository extends CrudRepository<UrlRedis, String> {
}
