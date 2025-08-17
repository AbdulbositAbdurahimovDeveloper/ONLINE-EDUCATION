package uz.pdp.online_education.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.pdp.online_education.payload.faq.FaqDTO;
import uz.pdp.online_education.payload.faq.FaqRequestDTO;
import uz.pdp.online_education.payload.ResponseDTO;
import uz.pdp.online_education.service.interfaces.FaqService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/faqs")
@RequiredArgsConstructor
public class FaqsController {

    private final FaqService faqService;

    @Operation(
            summary = "Get all FAQs",
            description = "Fetches the list of all frequently asked questions (public endpoint).",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of FAQs retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                                            {
                                              "success": true,
                                              "data": [
                                                {"id": 1, "question": "How to reset password?", "answer": "Click 'Forgot password'..."},
                                                {"id": 2, "question": "How to enroll in a course?", "answer": "Go to course page and press Enroll"}
                                              ]
                                            }
                                            """)
                            )
                    )
            }
    )
    @GetMapping
    public ResponseEntity<ResponseDTO<List<FaqDTO>>> getAll() {
        return ResponseEntity.ok(ResponseDTO.success(faqService.getAll()));
    }

    @Operation(
            summary = "Get FAQ by ID",
            description = "Fetches a specific FAQ by its ID (public endpoint).",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "FAQ retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "id": 1,
                                                "question": "How to reset password?",
                                                "answer": "Click 'Forgot password'..."
                                              }
                                            }
                                            """)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "FAQ not found")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<FaqDTO>> getById(
            @Parameter(description = "FAQ ID", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(ResponseDTO.success(faqService.getById(id)));
    }

    @Operation(
            summary = "Create a new FAQ",
            description = "Creates a new FAQ. Accessible only by ADMIN or INSTRUCTOR.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "FAQ data",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "question": "How to apply for a refund?",
                                      "answer": "Contact support within 7 days."
                                    }
                                    """)
                    )
            )
    )
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDTO<FaqDTO>> create(@Valid @RequestBody FaqRequestDTO dto) {
        return ResponseEntity.ok(ResponseDTO.success(faqService.create(dto)));
    }

    @Operation(
            summary = "Update an FAQ",
            description = "Updates an existing FAQ. Accessible only by ADMIN or INSTRUCTOR."
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDTO<FaqDTO>> update(
            @Parameter(description = "FAQ ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody FaqRequestDTO dto) {
        return ResponseEntity.ok(ResponseDTO.success(faqService.update(id, dto)));
    }

    @Operation(
            summary = "Delete an FAQ",
            description = "Deletes a specific FAQ by ID. Accessible only by ADMIN or INSTRUCTOR.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "FAQ deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "FAQ not found")
            }
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDTO<String>> delete(
            @Parameter(description = "FAQ ID", example = "1") @PathVariable Long id) {
        faqService.delete(id);
        return ResponseEntity.ok(ResponseDTO.success("Faq deleted"));
    }

    @Operation(
            summary = "Swap FAQ display order",
            description = "Changes the display order of an FAQ by providing a new order value."
    )
    @PatchMapping("/swap-order")
    public ResponseEntity<ResponseDTO<String>> swapDisplayOrder(
            @Parameter(description = "FAQ ID", example = "1") @RequestParam Long faqId,
            @Parameter(description = "New display order", example = "3") @RequestParam int newDisplayOrder) {
        faqService.swapDisplayOrder(faqId, newDisplayOrder);
        return ResponseEntity.ok(ResponseDTO.success("Display orders swapped successfully"));
    }

}
