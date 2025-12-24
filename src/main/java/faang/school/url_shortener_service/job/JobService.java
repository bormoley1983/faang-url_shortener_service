package faang.school.url_shortener_service.job;

import faang.school.url_shortener_service.hash.HashGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JobService {
    private final HashGenerator hashGenerator;

    @Scheduled(cron = "*/05 * * * * ?")
    public void checkHashInBd() {
        hashGenerator.scheduler();
    }
}