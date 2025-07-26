package uz.pdp.online_education.mapper;

import org.mapstruct.*;
import uz.pdp.online_education.model.Category;
import uz.pdp.online_education.payload.category.CategoryCreateDTO;
import uz.pdp.online_education.payload.category.CategoryDTO;
import uz.pdp.online_education.payload.category.CategoryUpdateDTO;

import java.sql.Timestamp;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    Category toEntity(CategoryCreateDTO dto);

    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "timestampToMillis")
    @Mapping(source = "updatedAt", target = "updatedAt", qualifiedByName = "timestampToMillis")
    CategoryDTO toDTO(Category category);

    void update(@MappingTarget Category category, CategoryUpdateDTO dto);

    @Named("timestampToMillis")
    static Long mapTimestampToMillis(Timestamp timestamp) {
        return timestamp != null ? timestamp.getTime() : null;
    }
}
