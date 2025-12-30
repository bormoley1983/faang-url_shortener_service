package faang.school.urlshortenerservice.hash;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.sqids.Sqids;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SqidsEncoder {

    private final Sqids sqids;

    public List<String> encode(List<Long> numbers) {
        return numbers.stream()
                .filter(n -> n != null && n >= 0)
                .map(this::encodeLong)
                .toList();
    }

    private String encodeLong(Long value) {
        return sqids.encode(List.of(value));
    }
}
