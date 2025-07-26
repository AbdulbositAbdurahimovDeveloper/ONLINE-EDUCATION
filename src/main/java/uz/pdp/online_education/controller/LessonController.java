package uz.pdp.online_education.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    @GetMapping
    public Page<LessonResponseDTO> read(@RequestParam(defaultValue = "0") Integer page,
                                        @RequestParam(defaultValue = "10") Integer size
    ) {
        return lessonService.read(page, size);
    }

    @GetMapping("/{id}")
    public LessonResponseDTO read(@PathVariable("id") Long id) {
        return lessonService.read(id);
    }

    @PostMapping
    public LessonResponseDTO create(@RequestBody LessonCreatDTO lessonCreatDTO) {
        return lessonService.create(lessonCreatDTO);
    }

    @PutMapping("/{id}")
    public LessonResponseDTO update(@PathVariable("id") Long id,
                                    @RequestBody LessonUpdateDTO lessonUpdateDTO) {
        return lessonService.update(id, lessonUpdateDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        lessonService.delete(id);
        return ResponseEntity.ok("Lesson deleted successfully");
    }

    @PatchMapping("/order/{moduleId}")
    public ResponseEntity<Void> updateLessonOrder(
            @PathVariable Long moduleId,
            @Valid @RequestBody LessonOrderUpdateDTO request) { // Endi DTO qabul qilamiz

        // Servisga DTO'ning ichidagi ro'yxatni uzatamiz
        lessonService.updateOrder(moduleId, request.getOrderedIds());
        return ResponseEntity.ok().build();
    }
}
