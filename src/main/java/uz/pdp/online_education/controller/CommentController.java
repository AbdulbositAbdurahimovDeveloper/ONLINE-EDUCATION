package uz.pdp.online_education.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.payload.ResponseDTO;
import uz.pdp.online_education.payload.comment.CommentResponseDto;
import uz.pdp.online_education.payload.comment.CommentUpdateDto;
import uz.pdp.online_education.service.interfaces.CommentService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;


    @Operation(summary = "Update a comment", description = "Updates an existing comment. Only the comment owner can update.")
    @PutMapping("/{id}")
    public ResponseEntity<ResponseDTO<CommentResponseDto>> update(
            @Parameter(description = "Comment ID", example = "10") @PathVariable Long id,
            @RequestBody CommentUpdateDto updateDto,
            @AuthenticationPrincipal User user) {
        CommentResponseDto responseDto = commentService.update(id, updateDto, user.getId());
        return ResponseEntity.ok(ResponseDTO.success(responseDto));
    }


    @Operation(summary = "Delete a comment", description = "Deletes a comment. Only the comment owner can delete.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDTO<String>> delete(
            @Parameter(description = "Comment ID", example = "10") @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        commentService.delete(id, user.getId());
        return ResponseEntity.ok(ResponseDTO.success("Comment deleted"));
    }


    @Operation(summary = "Get all root-level comments", description = "Fetch all top-level comments without replies.")
    @GetMapping
    public ResponseEntity<ResponseDTO<List<CommentResponseDto>>> getAll() {
        List<CommentResponseDto> allComments = commentService.getAll();
        return ResponseEntity.ok(ResponseDTO.success(allComments));
    }


    @Operation(summary = "Get all comments by course", description = "Fetch all comments related to a specific course.")
    @GetMapping("/course/{courseId}")
    public ResponseEntity<ResponseDTO<List<CommentResponseDto>>> getAllByCourseId(
            @Parameter(description = "Course ID", example = "5") @PathVariable Long courseId) {
        List<CommentResponseDto> courseComments = commentService.getAllByCourseId(courseId);
        return ResponseEntity.ok(ResponseDTO.success(courseComments));
    }


    @Operation(summary = "Get all comments by lesson", description = "Fetch all comments related to a specific lesson.")
    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<ResponseDTO<List<CommentResponseDto>>> getAllByLessonId(
            @Parameter(description = "Lesson ID", example = "3") @PathVariable Long lessonId) {
        List<CommentResponseDto> lessonComments = commentService.getAllByLessonId(lessonId);
        return ResponseEntity.ok(ResponseDTO.success(lessonComments));
    }
}
