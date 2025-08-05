package uz.pdp.online_education.controller;

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

    @GetMapping("/open/courses/filter")
    public ResponseEntity<ResponseDTO<PageDTO<?>>> filter(FilterDTO filterDTO,
                                                          @RequestParam(defaultValue = "0") Integer page,
                                                          @RequestParam(defaultValue = "10") Integer size) {
        PageDTO<CourseDetailDTO> courseDetailDTOPageDTO = courseService.filter(filterDTO,page,size);
        return  ResponseEntity.ok(ResponseDTO.success(courseDetailDTOPageDTO));
    }

    @GetMapping("/open/courses")
    public ResponseEntity<ResponseDTO<PageDTO<CourseDetailDTO>>> read(@RequestParam(defaultValue = "0") Integer page,
                                                                      @RequestParam(defaultValue = "10") Integer size) {
        PageDTO<CourseDetailDTO> courseDetailDTO = courseService.read(page, size);
        return ResponseEntity.ok(ResponseDTO.success(courseDetailDTO));
    }

    @GetMapping("/open/courses/{id}")
    public ResponseEntity<ResponseDTO<CourseDetailDTO>> read(@PathVariable Long id) {
        CourseDetailDTO courseDetailDTO = courseService.read(id);
        return ResponseEntity.ok(ResponseDTO.success(courseDetailDTO));
    }

    @GetMapping("courses/{courseId}/modules")
    public ResponseEntity<ResponseDTO<PageDTO<ModuleDetailDTO>>> read(@PathVariable Long courseId,
                                                                      @RequestParam(defaultValue = "0") Integer page,
                                                                      @RequestParam(defaultValue = "10") Integer size) {
        PageDTO<ModuleDetailDTO> modulePage = moduleService.read(courseId, page, size);

        return ResponseEntity.ok(ResponseDTO.success(modulePage));
    }

    @PostMapping("/courses")
    @PreAuthorize(value = "hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDTO<CourseDetailDTO>> create(@RequestBody @Valid CourseCreateDTO courseCreateDTO,
                                                               @AuthenticationPrincipal User instructor) {
        CourseDetailDTO courseDetailDTO = courseService.create(courseCreateDTO, instructor);
        return ResponseEntity.ok(ResponseDTO.success(courseDetailDTO));
    }

    @PutMapping("/courses/{id}")
    @PreAuthorize(value = "hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDTO<CourseDetailDTO>> update(@PathVariable Long id,
                                                               @RequestBody @Valid CourseUpdateDTO courseUpdateDTO,
                                                               @AuthenticationPrincipal User instructor) {
        CourseDetailDTO courseDetailDTO = courseService.update(id, courseUpdateDTO, instructor);
        return ResponseEntity.ok(ResponseDTO.success(courseDetailDTO));
    }

    @PatchMapping("/courses/{id}")
    @PreAuthorize(value = "hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDTO<?>> patch(@PathVariable Long id) {
        courseService.updateSuccess(id);
        return ResponseEntity.ok(ResponseDTO.success("update"));
    }

    @DeleteMapping("/courses/{id}")
    @PreAuthorize(value = "hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDTO<String>> delete(@PathVariable Long id) {
        courseService.delete(id);
        return ResponseEntity.ok(ResponseDTO.success("Courses deleted"));
    }
}
