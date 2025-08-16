package uz.pdp.online_education.controller;

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



    // 2. Update comment
    @PutMapping("/{id}")
    public ResponseEntity<ResponseDTO<CommentResponseDto>> update(@PathVariable Long id,
                                                                  @RequestBody CommentUpdateDto updateDto,
                                                                  @AuthenticationPrincipal User user) {
        CommentResponseDto responseDto = commentService.update(id, updateDto, user.getId());
        return ResponseEntity.ok(ResponseDTO.success(responseDto));
    }

    // 3. Delete comment
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDTO<String>> delete(@PathVariable Long id,
                                                      @AuthenticationPrincipal User user) {
        commentService.delete(id, user.getId());
        return ResponseEntity.ok(ResponseDTO.success("Comment deleted"));
    }

    // 4. Get all comments (root level only)
    @GetMapping
    public ResponseEntity<ResponseDTO<List<CommentResponseDto>>> getAll() {
        List<CommentResponseDto> allComments = commentService.getAll();
        return ResponseEntity.ok(ResponseDTO.success(allComments));
    }

    // 5. Get all comments for a course
    @GetMapping("/course/{courseId}")
    public ResponseEntity<ResponseDTO<List<CommentResponseDto>>> getAllByCourseId(@PathVariable Long courseId) {
        List<CommentResponseDto> courseComments = commentService.getAllByCourseId(courseId);
        return ResponseEntity.ok(ResponseDTO.success(courseComments));
    }

    // 6. Get all comments for a lesson
    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<ResponseDTO<List<CommentResponseDto>>> getAllByLessonId(@PathVariable Long lessonId) {
        List<CommentResponseDto> lessonComments = commentService.getAllByLessonId(lessonId);
        return ResponseEntity.ok(ResponseDTO.success(lessonComments));
    }
}
