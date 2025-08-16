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
    import uz.pdp.online_education.payload.quiz.QuestionResponseDTO;
    import uz.pdp.online_education.payload.quiz.QuestionUpdateWithAnswersDTO;
    import uz.pdp.online_education.service.interfaces.QuestionService;

    @RestController
    @RequestMapping("/api/v1/questions")
    @RequiredArgsConstructor
    @Tag(name = "Question Controller", description = "APIs for managing quiz questions")
    public class QuestionController {

        private final QuestionService questionService;

        @GetMapping("{id}")
        @PreAuthorize(value = "hasRole('ADMIN') or hasRole('INSTRUCTOR') or @courseSecurity.isUserQuestionBought(authentication, #id)")
        @Operation(summary = "Get question by ID", description = "Retrieve a question along with its answers by ID")
        @ApiResponse(responseCode = "200", description = "Successfully retrieved question")
        public ResponseEntity<ResponseDTO<?>> getQuestionById(@PathVariable Long id) {
            QuestionResponseDTO questionResponseDTO = questionService.getById(id);
            return ResponseEntity.ok(ResponseDTO.success(questionResponseDTO));
        }

        @PutMapping("{id}")
        @PreAuthorize(value = "hasAnyRole('ADMIN','INSTRUCTOR')")
        @Operation(summary = "Update question with answers", description = "Update a question and its answers (Admin or Instructor only)")
        @ApiResponse(responseCode = "200", description = "Successfully updated question")
        public ResponseEntity<QuestionResponseDTO> updateQuestionWithAnswers(
                @PathVariable("id") Long questionId,
                @Valid @RequestBody QuestionUpdateWithAnswersDTO updateDTO) {
            return ResponseEntity.ok(questionService.updateWithAnswers(questionId, updateDTO));
        }

        @DeleteMapping("{id}")
        @PreAuthorize(value = "hasAnyRole('ADMIN','INSTRUCTOR')")
        @Operation(summary = "Delete question", description = "Delete a question by its ID (Admin or Instructor only)")
        @ApiResponse(responseCode = "200", description = "Successfully deleted question")
        public ResponseEntity<ResponseDTO<?>> deleteQuestion(@PathVariable Long id) {
            questionService.delete(id);
            return ResponseEntity.ok(ResponseDTO.success("Question deleted successfully"));
        }
    }
