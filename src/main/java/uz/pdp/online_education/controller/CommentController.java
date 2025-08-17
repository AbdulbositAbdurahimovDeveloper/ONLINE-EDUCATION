package uz.pdp.online_education.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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

/**
 * CommentController foydalanuvchilar tomonidan qoldirilgan izohlar (kommentlar) ustidan CRUD amallarni bajarish uchun javobgar.
 * Bu controller REST API endpointlarini taqdim etadi va xizmat (service) qatlamiga murojaat qiladi.
 * Asosan kurs va darslarga qoldirilgan kommentlarni boshqarish uchun ishlatiladi.
 */
@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * Kommentni yangilash (update qilish).
     * Faqat izoh egasi (comment owner) o‘z izohini yangilashi mumkin.
     *
     * @param id        Komment IDsi (PathVariable orqali olinadi)
     * @param updateDto Yangi ma’lumotlar (RequestBody orqali olinadi)
     * @param user      Hozir login qilgan foydalanuvchi (Spring Security orqali @AuthenticationPrincipal)
     * @return Yangilangan komment ma’lumotlari
     */
    @Operation(
            summary = "Update a comment",
            description = "Updates an existing comment. Only the comment owner can update this comment."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment updated successfully",
                    content = @Content(schema = @Schema(implementation = CommentResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden: User does not own this comment"),
            @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ResponseDTO<CommentResponseDto>> update(
            @Parameter(description = "Comment ID", example = "10") @PathVariable Long id,
            @RequestBody CommentUpdateDto updateDto,
            @AuthenticationPrincipal User user) {

        CommentResponseDto responseDto = commentService.update(id, updateDto, user.getId());
        return ResponseEntity.ok(ResponseDTO.success(responseDto));
    }

    /**
     * Kommentni o‘chirish.
     * Faqat egasi o‘chirishi mumkin.
     *
     * @param id   Komment IDsi
     * @param user Hozirgi foydalanuvchi
     * @return O‘chirilganlik haqida xabar
     */
    @Operation(
            summary = "Delete a comment",
            description = "Deletes a comment. Only the comment owner can delete this comment."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden: User does not own this comment"),
            @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDTO<String>> delete(
            @Parameter(description = "Comment ID", example = "10") @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        commentService.delete(id, user.getId());
        return ResponseEntity.ok(ResponseDTO.success("Comment deleted"));
    }

    /**
     * Root-level kommentlarni olish.
     * Ya’ni faqat yuqori darajadagi (reply bo‘lmagan) kommentlar qaytariladi.
     *
     * @return Barcha root-level kommentlar
     */
    @Operation(
            summary = "Get all root-level comments",
            description = "Fetch all top-level comments without replies. Replies are excluded."
    )
    @GetMapping
    public ResponseEntity<ResponseDTO<List<CommentResponseDto>>> getAll() {
        List<CommentResponseDto> allComments = commentService.getAll();
        return ResponseEntity.ok(ResponseDTO.success(allComments));
    }

    /**
     * Kurs bo‘yicha barcha kommentlarni olish.
     *
     * @param courseId Kurs IDsi
     * @return Shu kursga tegishli kommentlar
     */
    @Operation(
            summary = "Get all comments by course",
            description = "Fetch all comments related to a specific course by its ID."
    )
    @GetMapping("/course/{courseId}")
    public ResponseEntity<ResponseDTO<List<CommentResponseDto>>> getAllByCourseId(
            @Parameter(description = "Course ID", example = "5") @PathVariable Long courseId) {
        List<CommentResponseDto> courseComments = commentService.getAllByCourseId(courseId);
        return ResponseEntity.ok(ResponseDTO.success(courseComments));
    }

    /**
     * Dars bo‘yicha barcha kommentlarni olish.
     *
     * @param lessonId Dars IDsi
     * @return Shu darsga tegishli kommentlar
     */
    @Operation(
            summary = "Get all comments by lesson",
            description = "Fetch all comments related to a specific lesson by its ID."
    )
    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<ResponseDTO<List<CommentResponseDto>>> getAllByLessonId(
            @Parameter(description = "Lesson ID", example = "3") @PathVariable Long lessonId) {
        List<CommentResponseDto> lessonComments = commentService.getAllByLessonId(lessonId);
        return ResponseEntity.ok(ResponseDTO.success(lessonComments));
    }
}
