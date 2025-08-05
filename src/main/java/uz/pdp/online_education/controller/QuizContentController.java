package uz.pdp.online_education.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.pdp.online_education.payload.ResponseDTO;
import uz.pdp.online_education.payload.quiz.QuizContentCreateDTO;
import uz.pdp.online_education.payload.quiz.QuizContentResponseDTO;
import uz.pdp.online_education.payload.quiz.QuizUpdateDTO;
import uz.pdp.online_education.service.interfaces.QuizContentService;

@RestController
@RequestMapping("/api/v1/quiz-content")
@RequiredArgsConstructor
public class QuizContentController {

    private final QuizContentService quizContentService;

    @GetMapping
    @PreAuthorize(value = "hasRole('ADMIN') or hasRole('INSTRUCTOR') or @courseSecurity.isUserModuleBought(authentication, #contentId)")
    public ResponseEntity<ResponseDTO<?>> getQuizContent(@RequestParam Long contentId) {
        QuizContentResponseDTO quizContentResponseDTO = quizContentService.getById(contentId);
        return ResponseEntity.ok(ResponseDTO.success(quizContentResponseDTO));
    }

    @PostMapping
    @PreAuthorize(value = "hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDTO<?>> createQuizContent(QuizContentCreateDTO quizContentCreateDTO) {
        QuizContentResponseDTO quizContentResponseDTO = quizContentService.create(quizContentCreateDTO);
        return ResponseEntity.ok(ResponseDTO.success(quizContentResponseDTO));
    }

    @PutMapping("/{contentId}")
    @PreAuthorize(value = "hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDTO<?>> updateQuizContent(@PathVariable Long contentId, @RequestBody QuizUpdateDTO quizUpdateDTO) {
        QuizContentResponseDTO updatedQuizContent = quizContentService.update(contentId, quizUpdateDTO);
        return ResponseEntity.ok(ResponseDTO.success(updatedQuizContent));
    }

    @DeleteMapping("/{contentId}")
    @PreAuthorize(value = "hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDTO<?>> deleteQuizContent(@PathVariable Long contentId) {
        quizContentService.delete(contentId);
        return ResponseEntity.ok(ResponseDTO.success("Quiz content deleted successfully"));
    }
}
