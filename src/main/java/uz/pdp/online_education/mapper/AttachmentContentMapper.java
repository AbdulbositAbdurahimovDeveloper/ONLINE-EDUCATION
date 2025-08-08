package uz.pdp.online_education.mapper;

import org.mapstruct.*;
import uz.pdp.online_education.model.lesson.AttachmentContent;
import uz.pdp.online_education.payload.attachment.AttachmentContentSummaryDTO;
import uz.pdp.online_education.payload.content.ContentDTO;
import uz.pdp.online_education.payload.lesson.AttachmentContentDTO;

import java.sql.Timestamp;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface AttachmentContentMapper {

    @Mapping(target = "contentUrl",source = "id")
    @Mapping(target = "lessonId",source = "lesson.id")
    @Mapping(target = "attachmentId",source = "attachment.id")
    @Mapping(target = "contentType", expression = "java(\"ATTACHMENT\")")
    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "timestampToLong")
    @Mapping(source = "updatedAt", target = "updatedAt", qualifiedByName = "timestampToLong")
    AttachmentContentDTO toDTO(AttachmentContent attachmentContent);

    @Named("timestampToLong")
    default Long timestampToLong(Timestamp timestamp)    {
        return timestamp != null ? timestamp.getTime() : null;
    }

    @Mapping(target = "contentType", expression = "java(\"ATTACHMENT\")")
    @Mapping(source = "attachment.id", target = "attachmentId")
    @Mapping(source = "attachment.originalName", target = "attachmentOriginalName")
    AttachmentContentSummaryDTO toAttachmentContentDTO(AttachmentContent ac);

}