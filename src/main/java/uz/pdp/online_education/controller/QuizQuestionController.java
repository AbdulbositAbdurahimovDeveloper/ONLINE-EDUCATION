package uz.pdp.online_education.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.pdp.online_education.payload.ResponseDTO;
import uz.pdp.online_education.payload.quiz.QuestionCreateWithAnswersDTO;
import uz.pdp.online_education.payload.quiz.QuestionResponseDTO;
import uz.pdp.online_education.service.interfaces.QuestionService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quiz-questions")
@RequiredArgsConstructor
public class QuizQuestionController {

    private final QuestionService questionService;


    @PostMapping
    @PreAuthorize(value = "hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDTO<?>> createQuestionWithAnswers(
            @Valid @RequestBody QuestionCreateWithAnswersDTO createDTO) {
        QuestionResponseDTO createdQuestion = questionService.createWithAnswers(createDTO);
        return ResponseEntity.ok(ResponseDTO.success(createdQuestion));
    }

    @GetMapping("{quizId}")
    @PreAuthorize(value = "hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDTO<?>> getAllQuestionsByQuizId(@PathVariable Long quizId) {
        List<QuestionResponseDTO> questions = questionService.getAllByQuizId(quizId);
        return ResponseEntity.ok(ResponseDTO.success(questions));
    }

}
