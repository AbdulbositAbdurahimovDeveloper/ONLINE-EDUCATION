package uz.pdp.online_education.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Quiz Content Controller", description = "APIs for managing quiz content")
public class QuizContentController {

    private final QuizContentService quizContentService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR') or @courseSecurity.isUserModuleBought(authentication, #contentId)")
    @Operation(
            summary = "Get quiz content",
            description = "Retrieve quiz content details by content ID",
            responses = @ApiResponse(responseCode = "200", description = "Quiz content retrieved successfully")
    )
    public ResponseEntity<ResponseDTO<QuizContentResponseDTO>> getQuizContent(@RequestParam Long contentId) {
        QuizContentResponseDTO quizContentResponseDTO = quizContentService.getById(contentId);
        return ResponseEntity.ok(ResponseDTO.success(quizContentResponseDTO));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @Operation(
            summary = "Create quiz content",
            description = "Create new quiz content for a module",
            responses = @ApiResponse(responseCode = "200", description = "Quiz content created successfully")
    )
    public ResponseEntity<ResponseDTO<QuizContentResponseDTO>> createQuizContent(
            @RequestBody QuizContentCreateDTO quizContentCreateDTO) {
        QuizContentResponseDTO quizContentResponseDTO = quizContentService.create(quizContentCreateDTO);
        return ResponseEntity.ok(ResponseDTO.success(quizContentResponseDTO));
    }

    @PutMapping("/{contentId}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @Operation(
            summary = "Update quiz content",
            description = "Update existing quiz content by its ID",
            responses = @ApiResponse(responseCode = "200", description = "Quiz content updated successfully")
    )
    public ResponseEntity<ResponseDTO<QuizContentResponseDTO>> updateQuizContent(
            @PathVariable Long contentId,
            @RequestBody QuizUpdateDTO quizUpdateDTO) {
        QuizContentResponseDTO updatedQuizContent = quizContentService.update(contentId, quizUpdateDTO);
        return ResponseEntity.ok(ResponseDTO.success(updatedQuizContent));
    }

    @DeleteMapping("/{contentId}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @Operation(
            summary = "Delete quiz content",
            description = "Delete quiz content by its ID",
            responses = @ApiResponse(responseCode = "200", description = "Quiz content deleted successfully")
    )
    public ResponseEntity<ResponseDTO<String>> deleteQuizContent(@PathVariable Long contentId) {
        quizContentService.delete(contentId);
        return ResponseEntity.ok(ResponseDTO.success("Quiz content deleted successfully"));
    }
}
