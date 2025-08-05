package uz.pdp.online_education.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.pdp.online_education.payload.PageDTO;
import uz.pdp.online_education.payload.ResponseDTO;
import uz.pdp.online_education.payload.lesson.LessonCreatDTO;
import uz.pdp.online_education.payload.lesson.LessonOrderUpdateDTO;
import uz.pdp.online_education.payload.lesson.LessonResponseDTO;
import uz.pdp.online_education.payload.lesson.LessonUpdateDTO;
import uz.pdp.online_education.service.interfaces.LessonService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lesson")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

//    @GetMapping("/module/{moduleId}")
//    public PageDTO<LessonResponseDTO> read(@PathVariable("moduleId") Long moduleId,
//                                           @RequestParam(defaultValue = "0") Integer page,
//                                           @RequestParam(defaultValue = "10") Integer size
//    ) {
//        return lessonService.read(moduleId, page, size);
//    }

    @GetMapping("/{id}")
    @PreAuthorize(value = "hasRole('ADMIN') or hasRole('INSTRUCTOR') or @courseSecurity.isPaymentOrFreeLesson(authentication, #id)")
    public ResponseEntity<ResponseDTO<?>> read(@PathVariable("id") Long id) {
        LessonResponseDTO lessonResponseDTO = lessonService.read(id);
        return ResponseEntity.ok(ResponseDTO.success(lessonResponseDTO));
    }

    @PostMapping
    @PreAuthorize(value = "hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDTO<?>> create(@RequestBody @Valid LessonCreatDTO lessonCreatDTO) {
        LessonResponseDTO lessonResponseDTO = lessonService.create(lessonCreatDTO);
        return ResponseEntity.ok(ResponseDTO.success(lessonResponseDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize(value = "hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDTO<?>> update(@PathVariable("id") Long id,
                                    @RequestBody @Valid LessonUpdateDTO lessonUpdateDTO) {
        LessonResponseDTO lessonResponseDTO = lessonService.update(id, lessonUpdateDTO);
        return ResponseEntity.ok(ResponseDTO.success(lessonResponseDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(value = "hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        lessonService.delete(id);
        return ResponseEntity.ok(ResponseDTO.success("Lesson deleted successfully"));
    }

    @PatchMapping("/order/{moduleId}")
    @PreAuthorize(value = "hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDTO<?>> updateLessonOrder(
            @PathVariable Long moduleId,
            @Valid @RequestBody LessonOrderUpdateDTO request) { // Endi DTO qabul qilamiz

        // Servisga DTO'ning ichidagi ro'yxatni uzatamiz
        lessonService.updateOrder(moduleId, request.getOrderedIds());
        return ResponseEntity.ok(ResponseDTO.success("Lesson order updated successfully"));
    }
}
