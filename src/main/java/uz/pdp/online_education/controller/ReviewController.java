package uz.pdp.online_education.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.payload.ResponseDTO;
import uz.pdp.online_education.payload.review.ReviewCreateDTO;
import uz.pdp.online_education.payload.review.ReviewDTO;
import uz.pdp.online_education.payload.review.ReviewUpdateDTO;
import uz.pdp.online_education.service.interfaces.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Review Controller", description = "APIs for managing course reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(
            summary = "Create review",
            description = "Create a new review for a course by the authenticated student",
            responses = @ApiResponse(responseCode = "200", description = "Review created successfully")
    )
    public ResponseEntity<ResponseDTO<ReviewDTO>> create(
            @RequestBody ReviewCreateDTO dto,
            @AuthenticationPrincipal User currentUser) {
        ReviewDTO reviewDTO = reviewService.create(dto, currentUser);
        return ResponseEntity.ok(ResponseDTO.success(reviewDTO));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get review by ID",
            description = "Retrieve review details by its ID",
            responses = @ApiResponse(responseCode = "200", description = "Review retrieved successfully")
    )
    public ResponseEntity<ResponseDTO<ReviewDTO>> getById(@PathVariable Long id) {
        ReviewDTO reviewDTO = reviewService.getById(id);
        return ResponseEntity.ok(ResponseDTO.success(reviewDTO));
    }

    @GetMapping
    @Operation(
            summary = "Get all reviews",
            description = "Retrieve all reviews across all courses",
            responses = @ApiResponse(responseCode = "200", description = "Reviews retrieved successfully")
    )
    public ResponseEntity<ResponseDTO<List<ReviewDTO>>> getAll() {
        List<ReviewDTO> allReviews = reviewService.getAll();
        return ResponseEntity.ok(ResponseDTO.success(allReviews));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update review",
            description = "Update an existing review by its ID",
            responses = @ApiResponse(responseCode = "200", description = "Review updated successfully")
    )
    public ResponseEntity<ResponseDTO<ReviewDTO>> update(
            @PathVariable Long id,
            @RequestBody ReviewUpdateDTO dto) {
        ReviewDTO updatedReview = reviewService.update(id, dto);
        return ResponseEntity.ok(ResponseDTO.success(updatedReview));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete review",
            description = "Delete a review by its ID",
            responses = @ApiResponse(responseCode = "204", description = "Review deleted successfully")
    )
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reviewService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
