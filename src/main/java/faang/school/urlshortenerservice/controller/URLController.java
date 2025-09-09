package faang.school.urlshortenerservice.controller;

import faang.school.urlshortenerservice.dto.UrlRequest;
import faang.school.urlshortenerservice.service.URLService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/url")
@Slf4j
public class URLController {

    private final URLService service;
    @Value("${shortener.host}")
    private String host;

    @PostMapping
    public ResponseEntity<String> createHash(@RequestBody UrlRequest request) {
        log.info(request.getUrl());
        return ResponseEntity.ok(host + service.createHash(request.getUrl()));
    }

//    @GetMapping("/{hash}")
//    public ResponseEntity<String> getUrl(@PathVariable String hash) {
//        service.getUrl(hash);
//    }
}
