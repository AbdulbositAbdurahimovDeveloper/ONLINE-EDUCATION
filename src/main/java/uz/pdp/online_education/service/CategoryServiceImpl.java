// CategoryServiceImpl.java
package uz.pdp.online_education.service;

import com.github.slugify.Slugify;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.mapper.CategoryMapper;
import uz.pdp.online_education.model.Category;
import uz.pdp.online_education.payload.category.*;
import uz.pdp.online_education.repository.CategoryRepository;
import uz.pdp.online_education.service.interfaces.CategoryService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final String NodFoundExcption = "Category not found with id: ";
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final Slugify slugify = Slugify.builder().build();

    @Override
    @Transactional
    public CategoryDTO create(CategoryCreateDTO dto) {
        Category category = categoryMapper.toEntity(dto);
        category.setSlug(slugify.slugify(dto.getName()));
        categoryRepository.save(category);
        return categoryMapper.toDTO(category);
    }

    @Override
    public CategoryDTO read(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(NodFoundExcption + id));
        return categoryMapper.toDTO(category);
    }

    @Override
    @Transactional
    public CategoryDTO update(Long id, CategoryUpdateDTO dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(NodFoundExcption + id));
        categoryMapper.update(category,dto);
        category.setSlug(slugify.slugify(dto.getName()));
        return categoryMapper.toDTO(category);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(NodFoundExcption + id));
        categoryRepository.delete(category);
    }

    @Override
    public List<CategoryDTO> getAll() {
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toDTO)
                .collect(Collectors.toList());
    }
//endi bu servicni ozgartirib ber qanday misol 0dan 5 gacha orderdisplayli faqlar bor ularni ichidan 2 ni ochirib yuborsak 3da gi desplayorder 2 ga 4 dagi 3 ga 5 dagi 4 ga tushisin faq chegaralanmagan yani kop qoshila veradi shuni inobatga olib qoy
}
