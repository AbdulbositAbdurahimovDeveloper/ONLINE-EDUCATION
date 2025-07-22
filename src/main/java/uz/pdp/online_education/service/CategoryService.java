package uz.pdp.online_education.service;

import uz.pdp.online_education.payload.CategoryDTO;

import java.util.List;

public interface CategoryService {
    CategoryDTO create(CategoryDTO dto);

    CategoryDTO read(Long id);

    CategoryDTO update(Long id, CategoryDTO dto);

    void delete(Long id);

    List<CategoryDTO> getAll();


}