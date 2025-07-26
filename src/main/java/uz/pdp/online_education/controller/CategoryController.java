// CategoryController.java
package uz.pdp.online_education.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.online_education.payload.ResponseDTO;
import uz.pdp.online_education.payload.category.*;
import uz.pdp.online_education.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<ResponseDTO<CategoryDTO>> create(@RequestBody CategoryCreateDTO dto) {
        return ResponseEntity.ok(ResponseDTO.success(categoryService.create(dto)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<CategoryDTO>> read(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseDTO.success(categoryService.read(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseDTO<CategoryDTO>> update(@PathVariable Long id, @RequestBody CategoryUpdateDTO dto) {
        return ResponseEntity.ok(ResponseDTO.success(categoryService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDTO<String>> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.ok(ResponseDTO.success("Category deleted successfully"));
    }

    @GetMapping
    public ResponseEntity<ResponseDTO<List<CategoryDTO>>> getAll() {
        return ResponseEntity.ok(ResponseDTO.success(categoryService.getAll()));
    }
}
