// CategoryServiceImpl.java
package uz.pdp.online_education.service;

import com.github.slugify.Slugify;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.online_education.exceptions.DataConflictException;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.mapper.CategoryMapper;
import uz.pdp.online_education.mapper.CourseMapper;
import uz.pdp.online_education.model.Category;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.payload.PageDTO;
import uz.pdp.online_education.payload.category.*;
import uz.pdp.online_education.payload.course.CourseDetailDTO;
import uz.pdp.online_education.repository.AttachmentRepository;
import uz.pdp.online_education.repository.CategoryRepository;
import uz.pdp.online_education.repository.CourseRepository;
import uz.pdp.online_education.service.interfaces.CategoryService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final String NotFoundException = "Category not found with id: ";
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final Slugify slugify = Slugify.builder().build();
    private final AttachmentRepository attachmentRepository;
    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;

    @Override
    @Transactional
    public CategoryDTO create(CategoryCreateDTO dto) {

        Category category = new Category();
        if (categoryRepository.existsByName(dto.getName())) {
            throw new DataConflictException("category name already exists");
        }
        category.setName(dto.getName());

        if (!attachmentRepository.existsByMinioKey(dto.getIcon())) {
            throw new DataConflictException("minio key not found with: " + dto.getIcon());
        }
        category.setIcon(dto.getIcon());
        category.setSlug(slugify.slugify(dto.getName()));
        categoryRepository.save(category);
        return categoryMapper.toDTO(category);
    }


    @Override
    public CategoryDTO read(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(NotFoundException + id));
        return categoryMapper.toDTO(category);
    }

    @Override
    @Transactional
    public CategoryDTO update(Long id, CategoryUpdateDTO dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(NotFoundException + id));

        if (categoryRepository.existsByName(dto.getName())) {
            throw new DataConflictException("category name already exists");
        }

        category.setName(dto.getName());
        if (dto.getIcon() != null & attachmentRepository.existsByMinioKey(dto.getIcon())) {
            category.setIcon(dto.getIcon());
        }
        category.setSlug(slugify.slugify(dto.getName()));
        return categoryMapper.toDTO(category);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(NotFoundException + id));
        categoryRepository.delete(category);
    }

    @Override
    public List<CategoryDTO> getAll() {
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * @param id   Long
     * @param page Integer
     * @param size Integer
     * @return PageDTO
     */
    @Override
    public PageDTO<CourseDetailDTO> readCoursesByCategoryId(Long id, Integer page, Integer size) {

        categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(NotFoundException + id));

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Course> courses = courseRepository.findAllByCategoryIdOrderByAverageRatingDesc(id, pageRequest);

        return new PageDTO<>(
                courses.getContent().stream().map(courseMapper::courseToCourseDetailDTO).toList(),
                courses.getNumber(),
                courses.getSize(),
                courses.getTotalElements(),
                courses.getTotalPages(),
                courses.isLast(),
                courses.isFirst(),
                courses.getNumberOfElements(),
                courses.isEmpty()
        );

    }
}
