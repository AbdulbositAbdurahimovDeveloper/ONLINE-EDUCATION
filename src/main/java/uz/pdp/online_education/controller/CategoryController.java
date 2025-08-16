package uz.pdp.online_education.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.pdp.online_education.payload.PageDTO;
import uz.pdp.online_education.payload.ResponseDTO;
import uz.pdp.online_education.payload.category.*;
import uz.pdp.online_education.payload.course.CourseDetailDTO;
import uz.pdp.online_education.service.interfaces.CategoryService;

import java.util.List;

/**
 * CategoryController – Kurs kategoriyalarini boshqarish uchun API.
 * <p>
 * Ushbu controller quyidagi imkoniyatlarni taqdim etadi:
 * <ul>
 *     <li>Kategoriya yaratish</li>
 *     <li>Kategoriya ma’lumotlarini olish</li>
 *     <li>Kategoriya yangilash</li>
 *     <li>Kategoriya o‘chirish</li>
 *     <li>Kategoriyalar ro‘yxatini olish</li>
 *     <li>Kurslarni kategoriya bo‘yicha ko‘rish (pagination bilan)</li>
 * </ul>
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Create new category",
            description = "Creates a new category. Only ADMIN or INSTRUCTOR can access this endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category created successfully",
                    content = @Content(schema = @Schema(implementation = CategoryDTO.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Only ADMIN/INSTRUCTOR can create")
    })
    @PostMapping("/category")
    @PreAuthorize(value = "hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDTO<CategoryDTO>> create(@RequestBody CategoryCreateDTO dto) {
        log.info("Request to create category: {}", dto);
        CategoryDTO categoryDTO = categoryService.create(dto);
        log.debug("Category created with id={}", categoryDTO.getId());
        return ResponseEntity.ok(ResponseDTO.success(categoryDTO));
    }

    @Operation(summary = "Get category by ID", description = "Retrieve category details by its ID. Public endpoint.")
    @GetMapping("/open/category/{id}")
    public ResponseEntity<ResponseDTO<CategoryDTO>> read(
            @Parameter(description = "Category ID", example = "1") @PathVariable Long id) {
        log.info("Fetching category with id={}", id);
        return ResponseEntity.ok(ResponseDTO.success(categoryService.read(id)));
    }

    @Operation(summary = "Update category",
            description = "Updates an existing category. Only ADMIN or INSTRUCTOR can update.")
    @PutMapping("/category/{id}")
    @PreAuthorize(value = "hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDTO<CategoryDTO>> update(
            @Parameter(description = "Category ID", example = "1") @PathVariable Long id,
            @RequestBody CategoryUpdateDTO dto) {
        log.info("Request to update category id={} with data={}", id, dto);
        CategoryDTO categoryDTO = categoryService.update(id, dto);
        log.debug("Category id={} updated successfully", id);
        return ResponseEntity.ok(ResponseDTO.success(categoryDTO));
    }

    @Operation(summary = "Delete category",
            description = "Deletes a category by its ID. Only ADMIN can delete.")
    @DeleteMapping("/category/{id}")
    @PreAuthorize(value = "hasAnyRole('ADMIN')")
    public ResponseEntity<ResponseDTO<String>> delete(
            @Parameter(description = "Category ID", example = "1") @PathVariable Long id) {
        log.warn("Request to delete category id={}", id);
        categoryService.delete(id);
        return ResponseEntity.ok(ResponseDTO.success("Category deleted successfully"));
    }

    @Operation(summary = "Get all categories", description = "Retrieve all categories. Public endpoint.")
    @GetMapping("/open/category")
    public ResponseEntity<ResponseDTO<List<CategoryDTO>>> getAll() {
        log.info("Fetching all categories");
        return ResponseEntity.ok(ResponseDTO.success(categoryService.getAll()));
    }

    @Operation(summary = "Get courses by category",
            description = "Retrieve paginated courses belonging to a category. Public endpoint.")
    @GetMapping("/open/category/courses/{categoryId}")
    public ResponseEntity<ResponseDTO<PageDTO<CourseDetailDTO>>> readCoursesByCategoryId(
            @Parameter(description = "Category ID", example = "1") @PathVariable Long categoryId,
            @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10") Integer size) {

        log.info("Fetching courses for categoryId={} page={} size={}", categoryId, page, size);
        PageDTO<CourseDetailDTO> courseDetailDTO = categoryService.readCoursesByCategoryId(categoryId, page, size);
        return ResponseEntity.ok(ResponseDTO.success(courseDetailDTO));
    }
}
