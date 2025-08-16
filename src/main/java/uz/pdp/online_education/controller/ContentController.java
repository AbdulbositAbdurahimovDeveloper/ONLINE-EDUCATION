package uz.pdp.online_education.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.pdp.online_education.payload.content.ContentUpdateOrderRequestDTO;
import uz.pdp.online_education.service.interfaces.ContentService;

/**
 * ContentController barcha kontent bilan bog‘liq API endpointlarini boshqaradi.
 * Bu yerda asosan lesson tarkibidagi contentlarni tartiblash va o‘chirish amallari mavjud.
 */
@RestController
@RequestMapping("api/v1/content")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    /**
     * Lesson ichidagi barcha contentlarni qayta tartiblaydi.
     *
     * @param lessonId - Lesson ID (misol uchun: 13)
     * @param request  - Yangi tartibda joylashgan content IDlar ro‘yxati
     * @return Muvaffaqiyatli bajarilganligi haqida xabar
     */
    @Operation(
            summary = "Update content order in a lesson",
            description = "Reorders all contents in a specific lesson. Accessible only by ADMIN or INSTRUCTOR.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Content order updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request data"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - only ADMIN/INSTRUCTOR can access this endpoint"),
                    @ApiResponse(responseCode = "404", description = "Lesson not found")
            }
    )
    @PatchMapping("/{lessonId}/order")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<?> updateAllContentOrders(
            @Parameter(description = "Lesson ID", example = "13") @PathVariable Long lessonId,
            @Valid @RequestBody ContentUpdateOrderRequestDTO request) {

        contentService.updateAllContentOrders(lessonId, request.getContentIds());
        return ResponseEntity.ok("Content order updated successfully");
    }

    /**
     * Berilgan ID bo‘yicha contentni o‘chirish.
     *
     * @param contentId - Content ID (misol uchun: 42)
     * @return Muvaffaqiyatli o‘chirilganligi haqida xabar
     */
    @Operation(
            summary = "Delete content",
            description = "Deletes specific content by its ID. Accessible only by ADMIN or INSTRUCTOR."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Content deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - only ADMIN/INSTRUCTOR can access this endpoint"),
            @ApiResponse(responseCode = "404", description = "Content not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<?> deleteContent(
            @Parameter(description = "Content ID", example = "42") @PathVariable("id") Long contentId) {
        contentService.deleteContent(contentId);
        return ResponseEntity.ok("Content deleted successfully");
    }
}
