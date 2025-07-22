package uz.pdp.online_education.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.online_education.payload.CategoryDTO;
import uz.pdp.online_education.payload.ResponseDTO;
import uz.pdp.online_education.service.CategoryService;



import java.util.List;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<ResponseDTO<CategoryDTO>> create(@RequestBody CategoryDTO dto) {
        return ResponseEntity.ok(ResponseDTO.success(categoryService.create(dto)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<CategoryDTO>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseDTO.success(categoryService.read(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseDTO<CategoryDTO>> update(@PathVariable Long id, @RequestBody CategoryDTO dto) {
        return ResponseEntity.ok(ResponseDTO.success(categoryService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDTO<String>> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.ok(ResponseDTO.success("Deleted"));
    }

    @GetMapping
    public ResponseEntity<ResponseDTO<List<CategoryDTO>>> getAll() {
        return ResponseEntity.ok(ResponseDTO.success(categoryService.getAll()));
    }
}
