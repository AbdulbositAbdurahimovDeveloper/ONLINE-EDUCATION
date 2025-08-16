package uz.pdp.online_education.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.payload.FilterDTO;
import uz.pdp.online_education.payload.PageDTO;
import uz.pdp.online_education.payload.ResponseDTO;
import uz.pdp.online_education.payload.course.CourseCreateDTO;
import uz.pdp.online_education.payload.course.CourseDetailDTO;
import uz.pdp.online_education.payload.course.CourseUpdateDTO;
import uz.pdp.online_education.payload.module.ModuleDetailDTO;
import uz.pdp.online_education.service.interfaces.CourseService;
import uz.pdp.online_education.service.interfaces.ModuleService;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final ModuleService moduleService;


    @Operation(
            summary = "Filter courses",
            description = "Applies filters (category, price, rating, etc.) and returns paginated course list."
    )
    @GetMapping("/open/courses/filter")
    public ResponseEntity<ResponseDTO<PageDTO<?>>> filter(
            FilterDTO filterDTO,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageDTO<CourseDetailDTO> courseDetailDTOPageDTO = courseService.filter(filterDTO, page, size);
        return ResponseEntity.ok(ResponseDTO.success(courseDetailDTOPageDTO));
    }


    @Operation(summary = "Get all courses (public)", description = "Fetches a paginated list of all available courses.")
    @GetMapping("/open/courses")
    public ResponseEntity<ResponseDTO<PageDTO<CourseDetailDTO>>> read(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageDTO<CourseDetailDTO> courseDetailDTO = courseService.read(page, size);
        return ResponseEntity.ok(ResponseDTO.success(courseDetailDTO));
    }


    @Operation(summary = "Get course by ID (public)", description = "Fetches a specific course by ID.")
    @GetMapping("/open/courses/{id}")
    public ResponseEntity<ResponseDTO<CourseDetailDTO>> read(
            @Parameter(description = "Course ID", example = "7") @PathVariable Long id) {
        CourseDetailDTO courseDetailDTO = courseService.read(id);
        return ResponseEntity.ok(ResponseDTO.success(courseDetailDTO));
    }


    @Operation(summary = "Get modules of a course", description = "Fetches all modules of a specific course.")
    @GetMapping("courses/{courseId}/modules")
    public ResponseEntity<ResponseDTO<PageDTO<ModuleDetailDTO>>> read(
            @Parameter(description = "Course ID", example = "7") @PathVariable Long courseId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageDTO<ModuleDetailDTO> modulePage = moduleService.read(courseId, page, size);
        return ResponseEntity.ok(ResponseDTO.success(modulePage));
    }


    @Operation(summary = "Create a course", description = "Creates a new course. Accessible only by ADMIN or INSTRUCTOR.")
    @PostMapping("/courses")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDTO<CourseDetailDTO>> create(
            @Valid @RequestBody CourseCreateDTO courseCreateDTO,
            @AuthenticationPrincipal User instructor) {
        CourseDetailDTO courseDetailDTO = courseService.create(courseCreateDTO, instructor);
        return ResponseEntity.ok(ResponseDTO.success(courseDetailDTO));
    }


    @Operation(summary = "Update a course", description = "Updates course details. Accessible only by ADMIN or INSTRUCTOR.")
    @PutMapping("/courses/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDTO<CourseDetailDTO>> update(
            @Parameter(description = "Course ID", example = "7") @PathVariable Long id,
            @Valid @RequestBody CourseUpdateDTO courseUpdateDTO,
            @AuthenticationPrincipal User instructor) {
        CourseDetailDTO courseDetailDTO = courseService.update(id, courseUpdateDTO, instructor);
        return ResponseEntity.ok(ResponseDTO.success(courseDetailDTO));
    }


    @Operation(summary = "Mark course as successful", description = "Marks a course as successfully updated.")
    @PatchMapping("/courses/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDTO<?>> patch(
            @Parameter(description = "Course ID", example = "7") @PathVariable Long id) {
        courseService.updateSuccess(id);
        return ResponseEntity.ok(ResponseDTO.success("update"));
    }


    @Operation(summary = "Delete a course", description = "Deletes a course by ID. Accessible only by ADMIN or INSTRUCTOR.")
    @DeleteMapping("/courses/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDTO<String>> delete(
            @Parameter(description = "Course ID", example = "7") @PathVariable Long id) {
        courseService.delete(id);
        return ResponseEntity.ok(ResponseDTO.success("Course deleted"));
    }
}
