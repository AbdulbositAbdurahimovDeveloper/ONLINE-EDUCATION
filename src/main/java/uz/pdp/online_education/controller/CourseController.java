package uz.pdp.online_education.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.online_education.payload.CourseRequestDTO;
import uz.pdp.online_education.payload.CourseResponseDTO;
import uz.pdp.online_education.service.CourseService;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Slf4j
public class CourseController {

    private final CourseService courseService;

    // CREATE
    @PostMapping
    public ResponseEntity<CourseResponseDTO> create(@RequestBody CourseRequestDTO dto) {
        log.info("Request to create course: {}", dto.getTitle());
        CourseResponseDTO response = courseService.create(dto);
        return ResponseEntity.ok(response);
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<CourseResponseDTO> update(@PathVariable Long id,
                                                    @RequestBody CourseRequestDTO dto) {
        log.info("Request to update course id={}", id);
        CourseResponseDTO response = courseService.update(id, dto);
        return ResponseEntity.ok(response);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        log.warn("Request to delete course id={}", id);
        courseService.delete(id);
        return ResponseEntity.ok("Course deleted successfully");
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<CourseResponseDTO> getById(@PathVariable Long id) {
        log.info("Fetching course by id={}", id);
        CourseResponseDTO response = courseService.getById(id);
        return ResponseEntity.ok(response);
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<CourseResponseDTO>> getAll() {
        log.info("Fetching all courses");
        return ResponseEntity.ok(courseService.getAll());
    }

    // GET BY CATEGORY
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<CourseResponseDTO>> getByCategory(@PathVariable Long categoryId) {
        log.info("Fetching courses by categoryId={}", categoryId);
        return ResponseEntity.ok(courseService.getByCategoryId(categoryId));
    }

    // GET BY INSTRUCTOR
    @GetMapping("/instructor/{instructorId}")
    public ResponseEntity<List<CourseResponseDTO>> getByInstructor(@PathVariable Long instructorId) {
        log.info("Fetching courses by instructorId={}", instructorId);
        return ResponseEntity.ok(courseService.getByInstructorId(instructorId));
    }
}
