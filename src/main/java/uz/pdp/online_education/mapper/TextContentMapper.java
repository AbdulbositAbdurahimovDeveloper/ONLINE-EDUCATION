package uz.pdp.online_education.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import uz.pdp.online_education.model.lesson.TextContent;
import uz.pdp.online_education.payload.content.ContentDTO;
import uz.pdp.online_education.payload.text.TextContentResponseDTO;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface TextContentMapper {


    @Mapping(source = "lesson.id", target = "lessonId")
    TextContentResponseDTO toDTO(TextContent textContent);
}