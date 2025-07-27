package uz.pdp.online_education.mapper;

import org.mapstruct.*;
import uz.pdp.online_education.model.Faq;
import uz.pdp.online_education.payload.faq.FaqDTO;
import uz.pdp.online_education.payload.faq.FaqRequestDTO;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FaqMapper {

    Faq toEntity(FaqRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFaqFromDto(FaqRequestDTO dto, @MappingTarget Faq faq);

    FaqDTO toDto(Faq faq);

    List<FaqDTO> toDtoList(List<Faq> faqs);
}
