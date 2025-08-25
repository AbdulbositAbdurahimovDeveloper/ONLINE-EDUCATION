package uz.pdp.online_education.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import uz.pdp.online_education.model.ContactMessage;
import uz.pdp.online_education.payload.ContactMessageRequestDTO;
import uz.pdp.online_education.payload.ContactMessageResponseDTO;

/**
 * Created by: suhrob
 */

@Mapper(componentModel = "spring")
public interface ContactMessageMapper {

    ContactMessageResponseDTO toDto(ContactMessage message);

     ContactMessage toEntity(ContactMessageRequestDTO dto);

    void updateEntityFromDto(ContactMessageRequestDTO dto, @MappingTarget ContactMessage entity);
}