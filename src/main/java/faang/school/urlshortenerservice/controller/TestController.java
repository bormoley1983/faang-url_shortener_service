package faang.school.urlshortenerservice.controller;

import faang.school.urlshortenerservice.service.HashCache;
import faang.school.urlshortenerservice.service.HashGenerator;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class TestController {

    private final HashGenerator hashGenerator;
    private final HashCache hashCache;

    @GetMapping
    public String getHash() {
//        hashGenerator.generateBatch();
        return hashCache.get();
    }
}
