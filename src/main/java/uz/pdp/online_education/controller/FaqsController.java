    package uz.pdp.online_education.controller;

    import io.swagger.v3.oas.annotations.Operation;
    import io.swagger.v3.oas.annotations.Parameter;
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


        @Operation(summary = "Get all FAQs", description = "Fetches the list of all frequently asked questions (public endpoint).")
        @GetMapping
        public ResponseEntity<ResponseDTO<List<FaqDTO>>> getAll() {
            return ResponseEntity.ok(ResponseDTO.success(faqService.getAll()));
        }


        @Operation(summary = "Get FAQ by ID", description = "Fetches a specific FAQ by its ID (public endpoint).")
        @GetMapping("/{id}")
        public ResponseEntity<ResponseDTO<FaqDTO>> getById(
                @Parameter(description = "FAQ ID", example = "1") @PathVariable Long id) {
            return ResponseEntity.ok(ResponseDTO.success(faqService.getById(id)));
        }


        @Operation(summary = "Create a new FAQ", description = "Creates a new FAQ. Accessible only by ADMIN or INSTRUCTOR.")
        @PostMapping
        @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
        public ResponseEntity<ResponseDTO<FaqDTO>> create(
                @Valid @RequestBody FaqRequestDTO dto) {
            return ResponseEntity.ok(ResponseDTO.success(faqService.create(dto)));
        }


        @Operation(summary = "Update an FAQ", description = "Updates an existing FAQ. Accessible only by ADMIN or INSTRUCTOR.")
        @PutMapping("/{id}")
        @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
        public ResponseEntity<ResponseDTO<FaqDTO>> update(
                @Parameter(description = "FAQ ID", example = "1") @PathVariable Long id,
                @Valid @RequestBody FaqRequestDTO dto) {
            return ResponseEntity.ok(ResponseDTO.success(faqService.update(id, dto)));
        }


        @Operation(summary = "Delete an FAQ", description = "Deletes a specific FAQ by ID. Accessible only by ADMIN or INSTRUCTOR.")
        @DeleteMapping("/{id}")
        @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
        public ResponseEntity<ResponseDTO<String>> delete(
                @Parameter(description = "FAQ ID", example = "1") @PathVariable Long id) {
            faqService.delete(id);
            return ResponseEntity.ok(ResponseDTO.success("Faq deleted"));
        }


        @Operation(summary = "Swap FAQ display order", description = "Changes the display order of an FAQ by providing a new order value.")
        @PatchMapping("/swap-order")
        public ResponseEntity<ResponseDTO<String>> swapDisplayOrder(
                @Parameter(description = "FAQ ID", example = "1") @RequestParam Long faqId,
                @Parameter(description = "New display order", example = "3") @RequestParam int newDisplayOrder) {
            faqService.swapDisplayOrder(faqId, newDisplayOrder);
            return ResponseEntity.ok(ResponseDTO.success("Display orders swapped successfully"));
        }

    }
