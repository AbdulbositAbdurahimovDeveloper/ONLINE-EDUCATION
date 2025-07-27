package uz.pdp.online_education.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.online_education.payload.content.ContentUpdateOrderRequestDTO;
import uz.pdp.online_education.service.interfaces.ContentService;

@RestController
@RequestMapping("api/v1/content")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    @PatchMapping("/{lessonId}/order") // PATCH /api/v1/lessons/13/contents/order
    public ResponseEntity<?> updateAllContentOrders(
            @PathVariable Long lessonId,
            @Valid @RequestBody ContentUpdateOrderRequestDTO request) {

        contentService.updateAllContentOrders(lessonId, request.getContentIds());
        return ResponseEntity.ok("Content order updated successfully");
    }

    // Bu yerga umumiy delete endpoint'ini ham qo'shishimiz mumkin
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteContent(@PathVariable("id") Long contentId) {
        contentService.deleteContent(contentId);
        return ResponseEntity.ok("Content deleted successfully");
    }
}
