package faang.school.urlshortenerservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class HashGeneratorService {
    public void generateAndSaveHashesAsync() {
        log.info("Generating and saving hashes to DB");
    }
}
