package uz.pdp.online_education.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.payload.ResponseDTO;
import uz.pdp.online_education.payload.quiz.test.AnswerResultDTO;
import uz.pdp.online_education.payload.quiz.test.AnswerSubmissionDTO;
import uz.pdp.online_education.payload.quiz.test.QuizResultDTO;
import uz.pdp.online_education.payload.quiz.test.StartAttemptResponseDTO;
import uz.pdp.online_education.service.interfaces.QuizAttemptService;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Quiz Attempt Controller", description = "APIs for managing quiz attempts")
public class QuizAttemptController {

    private final QuizAttemptService quizAttemptService;

    @PostMapping("/quizzes/{quizId}/start")
    @Operation(
            summary = "Start quiz attempt",
            description = "Start a new quiz attempt for the given quiz ID",
            responses = @ApiResponse(responseCode = "200", description = "Quiz attempt started successfully")
    )
    public ResponseEntity<ResponseDTO<StartAttemptResponseDTO>> startQuiz(
            @PathVariable Long quizId,
            @AuthenticationPrincipal User currentUser) {
        StartAttemptResponseDTO response = quizAttemptService.startAttempt(quizId, currentUser);
        return ResponseEntity.ok(ResponseDTO.success(response));
    }

    @PostMapping("/attempts/{attemptId}/answer")
    @Operation(
            summary = "Submit answer",
            description = "Submit an answer for a quiz question within an active attempt",
            responses = @ApiResponse(responseCode = "200", description = "Answer submitted successfully")
    )
    public ResponseEntity<ResponseDTO<AnswerResultDTO>> submitAnswer(
            @PathVariable Long attemptId,
            @Valid @RequestBody AnswerSubmissionDTO submissionDTO,
            @AuthenticationPrincipal User currentUser) {
        AnswerResultDTO result = quizAttemptService.submitAnswer(attemptId, submissionDTO, currentUser);
        return ResponseEntity.ok(ResponseDTO.success(result));
    }

    @PostMapping("/attempts/{attemptId}/finish")
    @Operation(
            summary = "Finish attempt",
            description = "Finish a quiz attempt and calculate final results",
            responses = @ApiResponse(responseCode = "200", description = "Quiz attempt finished successfully")
    )
    public ResponseEntity<ResponseDTO<QuizResultDTO>> finishAttempt(
            @PathVariable Long attemptId,
            @AuthenticationPrincipal User currentUser) {
        QuizResultDTO result = quizAttemptService.finishAttempt(attemptId, currentUser);
        return ResponseEntity.ok(ResponseDTO.success(result));
    }

    @GetMapping("/attempts/{attemptId}/result")
    @Operation(
            summary = "Get attempt result",
            description = "Retrieve the final result of a finished quiz attempt",
            responses = @ApiResponse(responseCode = "200", description = "Attempt result retrieved successfully")
    )
    public ResponseEntity<ResponseDTO<QuizResultDTO>> getAttemptResult(
            @PathVariable Long attemptId,
            @AuthenticationPrincipal User currentUser) {
        QuizResultDTO result = quizAttemptService.getAttemptResult(attemptId, currentUser);
        return ResponseEntity.ok(ResponseDTO.success(result));
    }
}
