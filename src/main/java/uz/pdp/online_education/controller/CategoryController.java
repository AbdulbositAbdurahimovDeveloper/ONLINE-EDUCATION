// CategoryController.java
package uz.pdp.online_education.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.pdp.online_education.payload.PageDTO;
import uz.pdp.online_education.payload.ResponseDTO;
import uz.pdp.online_education.payload.category.*;
import uz.pdp.online_education.payload.course.CourseDetailDTO;
import uz.pdp.online_education.service.interfaces.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping("/category")
    @PreAuthorize(value = "hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDTO<CategoryDTO>> create(@RequestBody CategoryCreateDTO dto) {
        CategoryDTO categoryDTO = categoryService.create(dto);
        return ResponseEntity.ok(ResponseDTO.success(categoryDTO));
    }

    @GetMapping("/open/category/{id}")
    public ResponseEntity<ResponseDTO<CategoryDTO>> read(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseDTO.success(categoryService.read(id)));
    }

    @PutMapping("/category/{id}")
    @PreAuthorize(value = "hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDTO<CategoryDTO>> update(@PathVariable Long id, @RequestBody CategoryUpdateDTO dto) {
        CategoryDTO categoryDTO = categoryService.update(id, dto);
        return ResponseEntity.ok(ResponseDTO.success(categoryDTO));
    }

    @DeleteMapping("/category/{id}")
    @PreAuthorize(value = "hasAnyRole('ADMIN')")
    public ResponseEntity<ResponseDTO<String>> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.ok(ResponseDTO.success("Category deleted successfully"));
    }

    @GetMapping("/open/category")
    public ResponseEntity<ResponseDTO<List<CategoryDTO>>> getAll() {
        return ResponseEntity.ok(ResponseDTO.success(categoryService.getAll()));
    }

    @GetMapping("/open/category/courses/{categoryId}")
    public ResponseEntity<?> readCoursesByCategoryId(@PathVariable Long categoryId,
                                                     @RequestParam(defaultValue = "0") Integer page,
                                                     @RequestParam(defaultValue = "10") Integer size) {
        PageDTO<CourseDetailDTO> courseDetailDTO = categoryService.readCoursesByCategoryId(categoryId, page, size);
        return ResponseEntity.ok(ResponseDTO.success(courseDetailDTO));
    }
}
