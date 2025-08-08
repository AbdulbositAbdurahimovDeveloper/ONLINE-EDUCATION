package uz.pdp.online_education.controller;

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

    @GetMapping
    public ResponseEntity<ResponseDTO<List<FaqDTO>>> getAll() {
        return ResponseEntity.ok(ResponseDTO.success(faqService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<FaqDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseDTO.success(faqService.getById(id)));
    }

    @PostMapping
    @PreAuthorize(value = "hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDTO<FaqDTO>> create(@RequestBody @Valid FaqRequestDTO dto) {
        return ResponseEntity.ok(ResponseDTO.success(faqService.create(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize(value = "hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDTO<FaqDTO>> update(@PathVariable Long id, @RequestBody @Valid FaqRequestDTO dto) {
        return ResponseEntity.ok(ResponseDTO.success(faqService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(value = "hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDTO<String>> delete(@PathVariable Long id) {
        faqService.delete(id);
        return ResponseEntity.ok(ResponseDTO.success("Faq deleted"));
    }
    @PatchMapping("/swap-order")
    public ResponseEntity<ResponseDTO<String>> swapDisplayOrder(@RequestParam Long faqId,
                                                                @RequestParam int newDisplayOrder) {
        faqService.swapDisplayOrder(faqId, newDisplayOrder);
        return ResponseEntity.ok(ResponseDTO.success("Display orders swapped successfully"));
    }


}
