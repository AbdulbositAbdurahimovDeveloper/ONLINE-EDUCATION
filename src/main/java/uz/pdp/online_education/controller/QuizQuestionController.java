package uz.pdp.online_education.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.online_education.payload.ResponseDTO;
import uz.pdp.online_education.payload.errors.ErrorDTO;
import uz.pdp.online_education.payload.quiz.QuestionCreateDTO;
import uz.pdp.online_education.payload.quiz.QuestionResponseDTO;
import uz.pdp.online_education.service.interfaces.QuestionService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quizzes/{quizId}/questions")
@RequiredArgsConstructor
public class QuizQuestionController {

    private final QuestionService questionService;

    @GetMapping
    public ResponseEntity<ResponseDTO<?>> getAllQuestionsByQuizId(@PathVariable Long quizId) {
        List<QuestionResponseDTO> questions = questionService.getAllByQuizId(quizId);
        return ResponseEntity.ok(ResponseDTO.success(questions));
    }

    @PostMapping
    public ResponseEntity<ResponseDTO<?>> createQuestion(@PathVariable Long quizId,
                                                         @RequestBody @Valid QuestionCreateDTO questionDTO) {
       if (!questionDTO.getQuizId().equals(quizId)) {
            return ResponseEntity.badRequest().body(ResponseDTO.error(new ErrorDTO(
                    HttpStatus.BAD_REQUEST.value(),
                    "The quiz ID in the request body does not match the path variable."
            )));
        }
        QuestionResponseDTO createdQuestion = questionService.create(questionDTO);
        return ResponseEntity.ok(ResponseDTO.success(createdQuestion));
    }
}
