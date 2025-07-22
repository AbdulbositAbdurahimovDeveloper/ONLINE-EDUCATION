package uz.pdp.online_education.mapper;

import org.mapstruct.*;
import uz.pdp.online_education.payload.FaqDTO;
import uz.pdp.online_education.payload.FaqRequestDTO;
import uz.pdp.online_education.model.Faq;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface FaqMapper {

    FaqDTO toDto(Faq faq);

    Faq toEntity(FaqRequestDTO faqRequestDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFaqFromDto(FaqRequestDTO dto, @MappingTarget Faq faq);

    List<FaqDTO> toDtoList(List<Faq> faqList);
}
