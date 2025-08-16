    package uz.pdp.online_education.controller;

    import io.swagger.v3.oas.annotations.Operation;
    import io.swagger.v3.oas.annotations.Parameter;
    import jakarta.validation.Valid;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.access.prepost.PreAuthorize;
    import org.springframework.web.bind.annotation.*;
    import uz.pdp.online_education.payload.content.ContentUpdateOrderRequestDTO;
    import uz.pdp.online_education.service.interfaces.ContentService;

    @RestController
    @RequestMapping("api/v1/content")
    @RequiredArgsConstructor
    public class ContentController {

        private final ContentService contentService;


        @Operation(
                summary = "Update content order in a lesson",
                description = "Reorders all contents in a specific lesson. Accessible only by ADMIN or INSTRUCTOR."
        )
        @PatchMapping("/{lessonId}/order")
        @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
        public ResponseEntity<?> updateAllContentOrders(
                @Parameter(description = "Lesson ID", example = "13") @PathVariable Long lessonId,
                @Valid @RequestBody ContentUpdateOrderRequestDTO request) {

            contentService.updateAllContentOrders(lessonId, request.getContentIds());
            return ResponseEntity.ok("Content order updated successfully");
        }


        @Operation(
                summary = "Delete content",
                description = "Deletes specific content by its ID. Accessible only by ADMIN or INSTRUCTOR."
        )
        @DeleteMapping("/{id}")
        @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
        public ResponseEntity<?> deleteContent(
                @Parameter(description = "Content ID", example = "42") @PathVariable("id") Long contentId) {
            contentService.deleteContent(contentId);
            return ResponseEntity.ok("Content deleted successfully");
        }
    }
