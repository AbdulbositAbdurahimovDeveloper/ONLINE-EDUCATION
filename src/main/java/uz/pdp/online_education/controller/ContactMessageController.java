package uz.pdp.online_education.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.online_education.payload.ContactMessageRequestDTO;
import uz.pdp.online_education.payload.ContactMessageResponseDTO;
import uz.pdp.online_education.payload.ResponseDTO;
import uz.pdp.online_education.service.interfaces.ContactMessageService;

import java.util.List;

@RestController
@RequestMapping("/api/contact-messages")
@RequiredArgsConstructor
public class ContactMessageController {

    private final ContactMessageService contactMessageService;

    // ✅ Xabar yuborish
    @PostMapping
    public ResponseEntity<ResponseDTO<String>> create(@RequestBody ContactMessageRequestDTO dto) {
        contactMessageService.create(dto);
        return ResponseEntity.ok(ResponseDTO.success("Xabar muvaffaqiyatli yuborildi!"));
    }

    // ✅ Replied bo'lmagan barcha xabarlarni olish
    @GetMapping("/unreplied")
    public ResponseEntity<ResponseDTO<List<ContactMessageResponseDTO>>> getAllExceptReplied() {
        List<ContactMessageResponseDTO> result = contactMessageService.getAllExceptReplied();
        return ResponseEntity.ok(ResponseDTO.success(result));
    }

    // ✅ Barcha xabarlarni olish
    @GetMapping
    public ResponseEntity<ResponseDTO<List<ContactMessageResponseDTO>>> getAll() {
        List<ContactMessageResponseDTO> result = contactMessageService.getAll();
        return ResponseEntity.ok(ResponseDTO.success(result));
    }

    // ✅ ID bo‘yicha xabarni olish va ko‘rilgan deb belgilash
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<ContactMessageResponseDTO>> getByIdAndMarkRead(@PathVariable Long id) {
        ContactMessageResponseDTO dto = contactMessageService.getByIdAndMarkRead(id);
        return ResponseEntity.ok(ResponseDTO.success(dto));
    }

    // ✅ Xabarga javob yozish
    @PostMapping("/{id}/reply")
    public ResponseEntity<ResponseDTO<String>> replyToMessage(@PathVariable Long id,
                                                              @RequestBody String replyText) {
        contactMessageService.replyToMessage(id, replyText);
        return ResponseEntity.ok(ResponseDTO.success("Javob muvaffaqiyatli yuborildi!"));
    }

    // ✅ Xabarni tahrirlash (faqat egasi va REPLIED bo'lmagan bo'lsa)
    @PutMapping("/{id}")
    public ResponseEntity<ResponseDTO<String>> edit(@PathVariable Long id,
                                                    @RequestBody ContactMessageRequestDTO dto) {
        contactMessageService.edit(id, dto);
        return ResponseEntity.ok(ResponseDTO.success("Xabar tahrirlandi"));
    }
}
