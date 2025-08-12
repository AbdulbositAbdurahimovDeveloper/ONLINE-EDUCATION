package uz.pdp.online_education.controller;

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
public class QuizAttemptController {

    private final QuizAttemptService quizAttemptService;

    @PostMapping("/quizzes/{quizId}/start")
    public ResponseEntity<ResponseDTO<StartAttemptResponseDTO>> startQuiz(
            @PathVariable Long quizId,
            @AuthenticationPrincipal User currentUser) {
        StartAttemptResponseDTO response = quizAttemptService.startAttempt(quizId, currentUser);
        return ResponseEntity.ok(ResponseDTO.success(response));
    }


    @PostMapping("/attempts/{attemptId}/answer")
    public ResponseEntity<AnswerResultDTO> submitAnswer(
            @PathVariable Long attemptId,
            @Valid @RequestBody AnswerSubmissionDTO submissionDTO,
            @AuthenticationPrincipal User currentUser) {

        AnswerResultDTO result = quizAttemptService.submitAnswer(attemptId, submissionDTO, currentUser);
        return ResponseEntity.ok(result);
    }


    @PostMapping("/attempts/{attemptId}/finish")
    public ResponseEntity<QuizResultDTO> finishAttempt(
            @PathVariable Long attemptId,
            @AuthenticationPrincipal User currentUser) {
        QuizResultDTO result = quizAttemptService.finishAttempt(attemptId, currentUser);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/attempts/{attemptId}/result")
    public ResponseEntity<QuizResultDTO> getAttemptResult(
            @PathVariable Long attemptId,
            @AuthenticationPrincipal User currentUser) {
        QuizResultDTO result = quizAttemptService.getAttemptResult(attemptId, currentUser);
        return ResponseEntity.ok(result);
    }
}
