package uz.pdp.online_education.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import uz.pdp.online_education.model.Attachment;
import uz.pdp.online_education.payload.AttachmentDTO;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface AttachmentMapper {
    AttachmentDTO toDTO(Attachment attachment);
}