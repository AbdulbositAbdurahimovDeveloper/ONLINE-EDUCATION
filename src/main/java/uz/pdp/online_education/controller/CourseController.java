package uz.pdp.online_education.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.online_education.payload.CourseRequestDTO;
import uz.pdp.online_education.payload.CourseResponseDTO;
import uz.pdp.online_education.payload.ResponseDTO;
import uz.pdp.online_education.service.CourseService;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Slf4j
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    public ResponseEntity<ResponseDTO<CourseResponseDTO>> create(@RequestBody CourseRequestDTO dto) {
        log.info("Creating course: {}", dto.getTitle());
        return ResponseEntity.ok(ResponseDTO.success(courseService.create(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseDTO<CourseResponseDTO>> update(@PathVariable Long id,
                                                                 @RequestBody CourseRequestDTO dto) {
        log.info("Updating course id={}", id);
        return ResponseEntity.ok(ResponseDTO.success(courseService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDTO<String>> delete(@PathVariable Long id) {
        log.warn("Deleting course id={}", id);
        courseService.delete(id);
        return ResponseEntity.ok(ResponseDTO.success("Course deleted successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<CourseResponseDTO>> getById(@PathVariable Long id) {
        log.info("Getting course id={}", id);
        return ResponseEntity.ok(ResponseDTO.success(courseService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ResponseDTO<List<CourseResponseDTO>>> getAll() {
        log.info("Getting all courses");
        return ResponseEntity.ok(ResponseDTO.success(courseService.getAll()));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ResponseDTO<List<CourseResponseDTO>>> getByCategory(@PathVariable Long categoryId) {
        log.info("Getting courses by category id={}", categoryId);
        return ResponseEntity.ok(ResponseDTO.success(courseService.getByCategoryId(categoryId)));
    }

    @GetMapping("/instructor/{instructorId}")
    public ResponseEntity<ResponseDTO<List<CourseResponseDTO>>> getByInstructor(@PathVariable Long instructorId) {
        log.info("Getting courses by instructor id={}", instructorId);
        return ResponseEntity.ok(ResponseDTO.success(courseService.getByInstructorId(instructorId)));
    }
}
