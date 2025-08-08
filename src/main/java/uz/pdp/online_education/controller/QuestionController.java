package uz.pdp.online_education.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.pdp.online_education.payload.ResponseDTO;
import uz.pdp.online_education.payload.quiz.QuestionResponseDTO;
import uz.pdp.online_education.payload.quiz.QuestionUpdateDTO;
import uz.pdp.online_education.service.interfaces.QuestionService;

@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping("{id}")
    @PreAuthorize(value = "hasRole('ADMIN') or hasRole('INSTRUCTOR') or @courseSecurity.isUserQuestionBought(authentication, #id)")
    public ResponseEntity<ResponseDTO<?>> getQuestionById(@PathVariable Long id) {
        QuestionResponseDTO questionResponseDTO = questionService.getById(id);

        return ResponseEntity.ok(ResponseDTO.success(questionResponseDTO));
    }

    @PutMapping("{id}")
    @PreAuthorize(value = "hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDTO<?>> updateQuestion(@PathVariable Long id, @RequestBody @Valid QuestionUpdateDTO questionUpdateDTO) {
        QuestionResponseDTO updatedQuestion = questionService.update(id, questionUpdateDTO);

        return ResponseEntity.ok(ResponseDTO.success(updatedQuestion));
    }

    @DeleteMapping("{id}")
    @PreAuthorize(value = "hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDTO<?>> deleteQuestion(@PathVariable Long id) {
        questionService.delete(id);
        return ResponseEntity.ok(ResponseDTO.success("Question deleted successfully"));
    }
}
