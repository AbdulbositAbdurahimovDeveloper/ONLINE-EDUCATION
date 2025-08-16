    package uz.pdp.online_education.controller;

    import io.swagger.v3.oas.annotations.Operation;
    import io.swagger.v3.oas.annotations.responses.ApiResponse;
    import io.swagger.v3.oas.annotations.tags.Tag;
    import jakarta.validation.Valid;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.access.prepost.PreAuthorize;
    import org.springframework.security.core.annotation.AuthenticationPrincipal;
    import org.springframework.web.bind.annotation.*;
    import uz.pdp.online_education.model.User;
    import uz.pdp.online_education.payload.PageDTO;
    import uz.pdp.online_education.payload.payment.PaymentCreateDTO;
    import uz.pdp.online_education.payload.payment.PaymentDTO;
    import uz.pdp.online_education.payload.ResponseDTO;
    import uz.pdp.online_education.service.interfaces.PaymentService;

    @RestController
    @RequestMapping("/api/v1/payments")
    @RequiredArgsConstructor
    @Tag(name = "Payment Controller", description = "APIs for managing payments")
    public class PaymentController {

        private final PaymentService paymentService;

        @GetMapping
        @PreAuthorize(value = "hasRole('ADMIN')")
        @Operation(summary = "Get all payments", description = "Retrieve a paginated list of all payments (Admin only)")
        @ApiResponse(responseCode = "200", description = "Successfully retrieved payments")
        public ResponseEntity<ResponseDTO<PageDTO<PaymentDTO>>> read(@RequestParam(defaultValue = "0") Integer page,
                                                                     @RequestParam(defaultValue = "10") Integer size) {
            PageDTO<PaymentDTO> paymentDTO = paymentService.read(page, size);
            return ResponseEntity.ok(ResponseDTO.success(paymentDTO));
        }

        @GetMapping("/{id}")
        @Operation(summary = "Get payment by ID", description = "Retrieve a specific payment by its ID")
        @ApiResponse(responseCode = "200", description = "Successfully retrieved payment")
        public ResponseEntity<ResponseDTO<PaymentDTO>> read(@PathVariable Long id) {
            PaymentDTO paymentDTO = paymentService.read(id);
            return ResponseEntity.ok(ResponseDTO.success(paymentDTO));
        }

        @PostMapping
        @Operation(summary = "Create a payment", description = "Create a new payment (for the authenticated user)")
        @ApiResponse(responseCode = "200", description = "Successfully created payment")
        public ResponseEntity<ResponseDTO<PaymentDTO>> create(@RequestBody PaymentCreateDTO paymentCreateDTO,
                                                              @AuthenticationPrincipal User currentUser) {
            PaymentDTO paymentDTO = paymentService.create(paymentCreateDTO, currentUser);
            return ResponseEntity.ok(ResponseDTO.success(paymentDTO));
        }
    }
