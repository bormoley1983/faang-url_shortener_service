package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.dto.UrlRequestDto;
import faang.school.urlshortenerservice.model.Url;
import jakarta.servlet.http.HttpServletRequest;

public interface UrlService {
    String createShortUrl(UrlRequestDto dto);

    Url getOriginalUrl(String hash, HttpServletRequest request);
}
