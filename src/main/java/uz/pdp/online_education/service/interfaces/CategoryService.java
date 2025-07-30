package uz.pdp.online_education.service.interfaces;

import uz.pdp.online_education.payload.category.CategoryCreateDTO;
import uz.pdp.online_education.payload.category.CategoryUpdateDTO;
import uz.pdp.online_education.payload.category.CategoryDTO;
import uz.pdp.online_education.payload.course.CourseResponseDto;

import java.util.List;

public interface CategoryService {

    CategoryDTO create(CategoryCreateDTO dto);

    CategoryDTO read(Long id);

    CategoryDTO update(Long id, CategoryUpdateDTO dto);

    void delete(Long id);

    List<CategoryDTO> getAll();

    public List<CourseResponseDto> getCoursesSortedByReview(Long categoryId);

}
