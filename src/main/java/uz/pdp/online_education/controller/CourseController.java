package uz.pdp.online_education.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import uz.pdp.online_education.assembler.CourseModelAssembler;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.payload.ResponseDTO;
import uz.pdp.online_education.payload.course.CourseCreateDTO;
import uz.pdp.online_education.payload.course.CourseDetailDTO;
import uz.pdp.online_education.payload.course.CourseUpdateDTO;
import uz.pdp.online_education.service.CourseService;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final CourseModelAssembler courseModelAssembler;

    @GetMapping("/open/courses")
    public ResponseEntity<ResponseDTO<PagedModel<CourseDetailDTO>>> read(@RequestParam(defaultValue = "0") Integer page,
                                                                         @RequestParam(defaultValue = "10") Integer size,
                                                                         PagedResourcesAssembler<Course> assembler) {
        Page<Course> courseDetailDTO = courseService.read(page, size);

        PagedModel<CourseDetailDTO> courseDetailDTOS = assembler.toModel(courseDetailDTO, courseModelAssembler);

        return ResponseEntity.ok(ResponseDTO.success(courseDetailDTOS));
    }

    @GetMapping("/open/courses/{id}")
    public ResponseEntity<ResponseDTO<CourseDetailDTO>> read(@PathVariable Long id) {
        CourseDetailDTO courseDetailDTO = courseService.read(id);
        return ResponseEntity.ok(ResponseDTO.success(courseDetailDTO));
    }

    @PostMapping("/courses")
    public ResponseEntity<ResponseDTO<CourseDetailDTO>> create(@RequestBody CourseCreateDTO courseCreateDTO,
                                                               @AuthenticationPrincipal User instructor) {
        CourseDetailDTO courseDetailDTO = courseService.create(courseCreateDTO, instructor);
        return ResponseEntity.ok(ResponseDTO.success(courseDetailDTO));
    }

    @PutMapping("/courses/{id}")
    public ResponseEntity<ResponseDTO<CourseDetailDTO>> update(@PathVariable Long id,
                                                               @RequestBody CourseUpdateDTO courseUpdateDTO,
                                                               @AuthenticationPrincipal User instructor) {
        CourseDetailDTO courseDetailDTO = courseService.update(id, courseUpdateDTO, instructor);
        return ResponseEntity.ok(ResponseDTO.success(courseDetailDTO));
    }

    @DeleteMapping("/courses/{id}")
    public ResponseEntity<ResponseDTO<String>> delete(@PathVariable Long id) {
        courseService.delete(id);
        return ResponseEntity.ok(ResponseDTO.success("Courses deleted"));
    }
}
