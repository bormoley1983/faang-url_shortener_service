package faang.school.urlshortenerservice.exception;

import faang.school.urlshortenerservice.entity.Hash;

public class UrlNotFoundException extends RuntimeException {
    public UrlNotFoundException(Hash hash) {
        super("URL с хешем " + hash.getHashValue() + " не найден в базе данных!");
    }
}
