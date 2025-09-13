package faang.school.urlshortenerservice.mapper;

import faang.school.urlshortenerservice.dto.CreateUrlDto;
import faang.school.urlshortenerservice.entity.Url;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UrlMapper {

    @Mapping(target = "hash", ignore = true)
    @Mapping(source = "url", target = "originalUrl")
    @Mapping(target = "createdAt", ignore = true)
    Url toUrl(CreateUrlDto createUrlDto);
}
