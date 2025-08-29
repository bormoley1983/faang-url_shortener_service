package faang.school.urlshortenerservice.controller;

import faang.school.urlshortenerservice.dto.UrlRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class UrlController {

    @GetMapping("/{hash}")
    public ResponseEntity<String> getUrlByHash(@PathVariable String hash) {
        return ResponseEntity.ok("We should return full url here.");
    }

    @PostMapping
    public ResponseEntity<String> generateHash(@RequestBody UrlRequestDto urlRequestDto) {
        return ResponseEntity.ok("We should return hash here.");
    }
}
