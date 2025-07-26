package uz.pdp.online_education.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.online_education.payload.ContactMessageRequestDTO;
import uz.pdp.online_education.payload.ContactMessageResponseDTO;
import uz.pdp.online_education.service.interfaces.ContactMessageService;

import java.util.List;

@RestController
@RequestMapping("/api/contact-messages")
@RequiredArgsConstructor
public class ContactMessageController {

    private final ContactMessageService contactMessageService;

    // Xabar yuborish
    @PostMapping
    public ResponseEntity<Void> create(@RequestBody ContactMessageRequestDTO dto) {
        contactMessageService.create(dto);
        return ResponseEntity.ok().build();
    }

    // Replied bo'lmagan barcha xabarlarni olish
    @GetMapping("/unreplied")
    public ResponseEntity<List<ContactMessageResponseDTO>> getAllExceptReplied() {
        return ResponseEntity.ok(contactMessageService.getAllExceptReplied());
    }

    // Barcha xabarlarni olish
    @GetMapping
    public ResponseEntity<List<ContactMessageResponseDTO>> getAll() {
        return ResponseEntity.ok(contactMessageService.getAll());
    }

    // ID bo‘yicha xabarni olish va uni ko‘rilgan deb belgilash
    @GetMapping("/{id}")
    public ResponseEntity<ContactMessageResponseDTO> getByIdAndMarkRead(@PathVariable Long id) {
        return ResponseEntity.ok(contactMessageService.getByIdAndMarkRead(id));
    }

    // Xabarga javob yozish
    @PostMapping("/{id}/reply")
    public ResponseEntity<String> replyToMessage(@PathVariable Long id, @RequestBody String replyText) {
        String htmlContent = contactMessageService.replyToMessage(id, replyText);
        return ResponseEntity.ok(htmlContent); // HTML qaytadi
    }

    // Xabarni tahrirlash (faqat egasi va REPLIED bo'lmagan bo'lsa)
    @PutMapping("/{id}")
    public ResponseEntity<Void> edit(@PathVariable Long id,
                                     @RequestParam String requesterEmail,
                                     @RequestBody ContactMessageRequestDTO dto) {
        contactMessageService.edit(id, requesterEmail, dto);
        return ResponseEntity.ok().build();
    }

}
