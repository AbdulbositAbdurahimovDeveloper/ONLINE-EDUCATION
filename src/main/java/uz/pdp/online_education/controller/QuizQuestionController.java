package uz.pdp.online_education.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Quiz Question Controller", description = "APIs for managing quiz questions with answer options")
public class QuizQuestionController {

    private final QuestionService questionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @Operation(
            summary = "Create quiz question with answers",
            description = "Create a new quiz question along with its answer options",
            responses = @ApiResponse(responseCode = "200", description = "Quiz question created successfully")
    )
    public ResponseEntity<ResponseDTO<QuestionResponseDTO>> createQuestionWithAnswers(
            @Valid @RequestBody QuestionCreateWithAnswersDTO createDTO) {
        QuestionResponseDTO createdQuestion = questionService.createWithAnswers(createDTO);
        return ResponseEntity.ok(ResponseDTO.success(createdQuestion));
    }

    @GetMapping("/{quizId}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @Operation(
            summary = "Get all questions by quiz ID",
            description = "Retrieve all questions and their answers for a specific quiz",
            responses = @ApiResponse(responseCode = "200", description = "Quiz questions retrieved successfully")
    )
    public ResponseEntity<ResponseDTO<List<QuestionResponseDTO>>> getAllQuestionsByQuizId(@PathVariable Long quizId) {
        List<QuestionResponseDTO> questions = questionService.getAllByQuizId(quizId);
        return ResponseEntity.ok(ResponseDTO.success(questions));
    }
}
