package faang.school.urlshortenerservice.mapper;

import faang.school.urlshortenerservice.dto.UrlRequestDto;
import faang.school.urlshortenerservice.dto.UrlResponseDto;
import faang.school.urlshortenerservice.model.Url;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface UrlMapper {
    Url toModel(UrlRequestDto dto);

    default UrlResponseDto toResponseDto(String shortUrl) {
        return new UrlResponseDto(shortUrl);
    }
}
