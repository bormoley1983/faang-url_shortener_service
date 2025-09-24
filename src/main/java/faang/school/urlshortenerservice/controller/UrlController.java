package faang.school.urlshortenerservice.controller;

import faang.school.urlshortenerservice.repository.HashRepository;
import faang.school.urlshortenerservice.service.HashCache;
import faang.school.urlshortenerservice.service.UrlService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/url")
public class UrlController {

    private final HashCache hashCache;
    private final UrlService urlService;
    private static final UrlValidator urlValidator = new UrlValidator(new String[]{"http", "https"}, UrlValidator.ALLOW_LOCAL_URLS);

    @PostMapping
    public ResponseEntity<String> createLink(@RequestBody String url) {
        if(!urlValidator.isValid(url)) {
            return ResponseEntity.badRequest().body("Invalid URL");
        }
        urlService.addLink(url);
        return ResponseEntity.ok(url);
    }

}
