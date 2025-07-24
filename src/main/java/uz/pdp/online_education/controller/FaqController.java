package uz.pdp.online_education.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.online_education.payload.FaqDTO;
import uz.pdp.online_education.payload.FaqRequestDTO;
import uz.pdp.online_education.service.interfaces.FaqService;

import java.util.List;

@RestController
@RequestMapping("/api/faqs")
@RequiredArgsConstructor
public class FaqController {

    private final FaqService faqService;

    // GET /api/faqs
    @GetMapping
    public ResponseEntity<List<FaqDTO>> getAll() {
        return ResponseEntity.ok(faqService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FaqDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(faqService.getById(id));
    }

    // POST /api/faqs
    @PostMapping
    public ResponseEntity<FaqDTO> create(@RequestBody FaqRequestDTO dto) {
        return ResponseEntity.ok(faqService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FaqDTO> update(@PathVariable Long id, @RequestBody FaqRequestDTO dto) {
        return ResponseEntity.ok(faqService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        faqService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
