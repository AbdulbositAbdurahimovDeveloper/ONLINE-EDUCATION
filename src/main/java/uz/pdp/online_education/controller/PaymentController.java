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
@Tag(name = "Payments", description = "APIs for managing student payments")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get all payments",
            description = "Retrieve a paginated list of all payments (Admin only)",
            responses = @ApiResponse(responseCode = "200", description = "Payments retrieved successfully")
    )
    public ResponseEntity<ResponseDTO<PageDTO<PaymentDTO>>> read(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageDTO<PaymentDTO> paymentDTO = paymentService.read(page, size);
        return ResponseEntity.ok(ResponseDTO.success(paymentDTO));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get payment by ID",
            description = "Retrieve a specific payment by its ID",
            responses = @ApiResponse(responseCode = "200", description = "Payment retrieved successfully")
    )
    public ResponseEntity<ResponseDTO<PaymentDTO>> read(@PathVariable Long id) {
        PaymentDTO paymentDTO = paymentService.read(id);
        return ResponseEntity.ok(ResponseDTO.success(paymentDTO));
    }

    @PostMapping
    @Operation(
            summary = "Create a payment",
            description = "Create a new payment (for the authenticated user)",
            responses = @ApiResponse(responseCode = "200", description = "Payment created successfully")
    )
    public ResponseEntity<ResponseDTO<PaymentDTO>> create(
            @RequestBody @Valid PaymentCreateDTO paymentCreateDTO,
            @AuthenticationPrincipal User currentUser) {
        PaymentDTO paymentDTO = paymentService.create(paymentCreateDTO, currentUser);
        return ResponseEntity.ok(ResponseDTO.success(paymentDTO));
    }
}
