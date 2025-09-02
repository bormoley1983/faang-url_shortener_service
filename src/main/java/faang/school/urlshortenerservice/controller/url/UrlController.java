package faang.school.urlshortenerservice.controller.url;

import faang.school.urlshortenerservice.dto.short_url.CreateShortUrlDto;
import faang.school.urlshortenerservice.dto.short_url.UrlDto;
import faang.school.urlshortenerservice.service.UrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;


@RequiredArgsConstructor
@RestController
public class UrlController {
    private final UrlService urlService;

    @Operation(
            description = "Generate short url",
            responses = {
                    @ApiResponse(responseCode = "201"),
            }
    )
    @PostMapping("/api/v1/url")
    public ResponseEntity<UrlDto> createShortUrl(@RequestBody @Valid CreateShortUrlDto dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new UrlDto(URI.create(urlService.createShortUrl(dto))));
    }
}