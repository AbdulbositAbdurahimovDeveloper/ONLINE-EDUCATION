package uz.pdp.online_education.controller;

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
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    @PreAuthorize(value = "hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<PageDTO<PaymentDTO>>> read(@RequestParam(defaultValue = "0") Integer page,
                                                                 @RequestParam(defaultValue = "10") Integer size) {
        PageDTO<PaymentDTO> paymentDTO = paymentService.read(page, size);
        return ResponseEntity.ok(ResponseDTO.success(paymentDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<PaymentDTO>> read(@PathVariable Long id) {
        PaymentDTO paymentDTO = paymentService.read(id);
        return ResponseEntity.ok(ResponseDTO.success(paymentDTO));
    }

    @PostMapping
    public  ResponseEntity<ResponseDTO<PaymentDTO>> create(@RequestBody PaymentCreateDTO paymentCreateDTO,
                                                           @AuthenticationPrincipal User currentUser) {
        PaymentDTO paymentDTO = paymentService.create(paymentCreateDTO,currentUser);
        return  ResponseEntity.ok(ResponseDTO.success(paymentDTO));
    }

}
