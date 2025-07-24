package uz.pdp.online_education.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.pdp.online_education.payload.ResponseDTO;

@RestController
@RequestMapping("/api/v1/lessons")
@RequiredArgsConstructor
public class LessonController {

    @GetMapping("/{lessonId}")
    public ResponseEntity<ResponseDTO<?>> read(@PathVariable Long lessonId) {

        return ResponseEntity.ok(ResponseDTO.success(""));
    }
}
