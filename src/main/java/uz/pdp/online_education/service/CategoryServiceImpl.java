package uz.pdp.online_education.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.mapper.CategoryMapper;
import uz.pdp.online_education.model.Category;
import uz.pdp.online_education.payload.CategoryDTO;
import uz.pdp.online_education.repository.CategoryRepository;
import uz.pdp.online_education.service.interfaces.CategoryService;

import java.util.List;


import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private static final String CATEGORY_NOT_FOUND = "Category not found with id: ";
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryDTO create(CategoryDTO dto) {
        Category category = categoryMapper.toEntity(dto);
        Category saved = categoryRepository.save(category);
        log.info("Created category with id: {}", saved.getId());
        return categoryMapper.toDTO(saved);
    }

    @Override
    public CategoryDTO read(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(CATEGORY_NOT_FOUND+ id));
        return categoryMapper.toDTO(category);
    }

    @Override
    public CategoryDTO update(Long id, CategoryDTO dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(CATEGORY_NOT_FOUND + id));
        category.setName(dto.getName());
        category.setIcon(dto.getIcon());
        category.setSlug(dto.getSlug());
        Category updated = categoryRepository.save(category);
        log.info("Updated category with id: {}", id);
        return categoryMapper.toDTO(updated);
    }

    @Override
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(CATEGORY_NOT_FOUND + id));
        categoryRepository.delete(category);
        log.info("Deleted category with id: {}", id);
    }

    @Override
    public List<CategoryDTO> getAll() {
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toDTO)
                .collect(Collectors.toList());
    }
}