package uz.pdp.online_education.mapper;



import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import uz.pdp.online_education.model.Category;
import uz.pdp.online_education.payload.CategoryDTO;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface CategoryMapper {

    CategoryDTO toDTO(Category category);

    Category toEntity(CategoryDTO dto);

}
