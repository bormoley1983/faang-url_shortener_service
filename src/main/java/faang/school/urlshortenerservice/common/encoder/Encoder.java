package faang.school.urlshortenerservice.common.encoder;

import java.util.List;

public interface Encoder {
    List<String> encode(List<Long> numbers);
}
