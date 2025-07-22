package uz.pdp.online_education.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.online_education.payload.FaqDTO;
import uz.pdp.online_education.payload.FaqRequestDTO;
import uz.pdp.online_education.payload.ResponseDTO;
import uz.pdp.online_education.service.FaqService;

import java.util.List;

@RestController
@RequestMapping("/api/faqs")
@RequiredArgsConstructor
public class FaqController {

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
    public ResponseEntity<ResponseDTO<FaqDTO>> create(@RequestBody FaqRequestDTO dto) {
        return ResponseEntity.ok(ResponseDTO.success(faqService.create(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseDTO<FaqDTO>> update(@PathVariable Long id, @RequestBody FaqRequestDTO dto) {
        return ResponseEntity.ok(ResponseDTO.success(faqService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDTO<String>> delete(@PathVariable Long id) {
        faqService.delete(id);
        return ResponseEntity.ok(ResponseDTO.success("Faq deleted"));
    }
}
