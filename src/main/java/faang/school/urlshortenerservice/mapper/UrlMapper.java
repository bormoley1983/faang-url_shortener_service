package faang.school.urlshortenerservice.mapper;

import faang.school.urlshortenerservice.dto.LongUrlDto;
import faang.school.urlshortenerservice.entity.Url;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UrlMapper {
    LongUrlDto toLongUrlDto(Url url);

    Url toUrl(LongUrlDto longUrlDto);
}
