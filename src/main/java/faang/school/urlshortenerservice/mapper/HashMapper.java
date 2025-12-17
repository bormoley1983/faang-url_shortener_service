package faang.school.urlshortenerservice.mapper;

import faang.school.urlshortenerservice.dto.ShortUrlDto;
import faang.school.urlshortenerservice.entity.Hash;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface HashMapper {

    ShortUrlDto shortUrlDto(Hash hash);
}
