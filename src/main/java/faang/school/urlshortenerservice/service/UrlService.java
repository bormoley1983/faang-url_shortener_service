package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.exception.InvalidUrlException;
import faang.school.urlshortenerservice.exception.UrlExpiredException;
import faang.school.urlshortenerservice.exception.UrlNotFoundException;
import faang.school.urlshortenerservice.model.Url;
import faang.school.urlshortenerservice.repository.UrlCacheRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class UrlService {
    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");
    private final UrlCacheRepository urlCacheRepository;
    private final UrlRepository urlRepository;
    private final HashCache hashCache;
    private final HostAddressResolver hostAddressResolver;

    @Value("${schedulers.config.cleanupOutdatedHashes.numberOfDaysForOutdatedHashes:365}")
    private int numberOfDaysForOutdatedHashes;

    @Value("${shortener.domain}")
    private String domain;

    @Value("${shortener.allow-private-network-targets:false}")
    private boolean allowPrivateNetworkTargets;

    @Transactional
    public String generateShortUrl(String url) {
        String normalizedUrl = normalizeAndValidate(url);
        log.info("Generating short URL for host: {}", extractHostForLog(normalizedUrl));

        String hash = hashCache.getNextHash();
        if (!StringUtils.hasText(hash)) {
            throw new RuntimeException("Failed to generate hash for URL");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusDays(numberOfDaysForOutdatedHashes);

        Url urlObject = Url.builder()
            .hash(hash)
            .url(normalizedUrl)
            .createdAt(now)
            .expiresAt(expiresAt)
            .build();
        Url savedUrl = urlRepository.save(urlObject);

        urlCacheRepository.saveUrl(hash, normalizedUrl, Duration.between(now, expiresAt));

        return domain + "/" + savedUrl.getHash();
    }

    @Transactional(readOnly = true)
    public String getUrl(String hash) {
        String result = urlCacheRepository.getUrl(hash);

        if (!StringUtils.hasText(result)) {
            Url url = urlRepository.findByHash(hash)
                    .orElseThrow(() -> new UrlNotFoundException(hash));

            if (!StringUtils.hasText(url.getUrl())) {
                throw new UrlNotFoundException(hash);
            }

            if (url.getExpiresAt() != null && !url.getExpiresAt().isAfter(LocalDateTime.now())) {
                urlCacheRepository.deleteByHash(hash);
                throw new UrlExpiredException(hash);
            }

            result = url.getUrl();
            if (url.getExpiresAt() != null) {
                Duration ttl = Duration.between(LocalDateTime.now(), url.getExpiresAt());
                if (!ttl.isNegative() && !ttl.isZero()) {
                    urlCacheRepository.saveUrl(hash, result, ttl);
                }
            }
        }

        return result;
    }

    private String normalizeAndValidate(String rawUrl) {
        if (!StringUtils.hasText(rawUrl)) {
            throw new InvalidUrlException("URL must not be blank");
        }

        String candidate = rawUrl.trim();
        if (candidate.startsWith("\"") && candidate.endsWith("\"") && candidate.length() >= 2) {
            candidate = candidate.substring(1, candidate.length() - 1).trim();
        }

        URI uri;
        try {
            uri = new URI(candidate);
        } catch (URISyntaxException ex) {
            throw new InvalidUrlException("URL is not a valid URI");
        }

        if (uri.isOpaque()) {
            throw new InvalidUrlException("URL must be hierarchical");
        }

        String scheme = uri.getScheme();
        if (!StringUtils.hasText(scheme)) {
            throw new InvalidUrlException("URL scheme is required");
        }

        String normalizedScheme = scheme.toLowerCase(Locale.ROOT);
        if (!ALLOWED_SCHEMES.contains(normalizedScheme)) {
            throw new InvalidUrlException("Only http and https URLs are allowed");
        }

        String host = uri.getHost();
        if (!StringUtils.hasText(host)) {
            throw new InvalidUrlException("URL host is required");
        }

        String normalizedHost = host.toLowerCase(Locale.ROOT);
        if ("localhost".equals(normalizedHost) || normalizedHost.endsWith(".local")) {
            throw new InvalidUrlException("Local hosts are not allowed");
        }

        if (!allowPrivateNetworkTargets) {
            validatePublicTarget(normalizedHost);
        }

        String normalized = uri.normalize().toString();
        if (normalized.length() > 2048) {
            throw new InvalidUrlException("URL must not exceed 2048 characters");
        }

        return normalized;
    }

    private void validatePublicTarget(String host) {
        try {
            for (InetAddress address : hostAddressResolver.resolve(host)) {
                if (isPrivateNetworkAddress(address)) {
                    throw new InvalidUrlException("Private network addresses are not allowed");
                }
            }
        } catch (UnknownHostException ex) {
            throw new InvalidUrlException("URL host cannot be resolved");
        }
    }

    private boolean isPrivateNetworkAddress(InetAddress address) {
        if (address.isAnyLocalAddress()
                || address.isLoopbackAddress()
                || address.isLinkLocalAddress()
                || address.isSiteLocalAddress()
                || address.isMulticastAddress()) {
            return true;
        }

        byte[] bytes = address.getAddress();
        if (bytes.length == 4) {
            int first = Byte.toUnsignedInt(bytes[0]);
            int second = Byte.toUnsignedInt(bytes[1]);
            return first == 100 && second >= 64 && second <= 127;
        }

        return bytes.length == 16 && (Byte.toUnsignedInt(bytes[0]) & 0xfe) == 0xfc;
    }

    private String extractHostForLog(String value) {
        try {
            return URI.create(value).getHost();
        } catch (Exception ex) {
            return "unknown";
        }
    }
}
