package uz.pdp.online_education.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.online_education.payload.ContactMessageRequestDTO;
import uz.pdp.online_education.payload.ContactMessageResponseDTO;
import uz.pdp.online_education.payload.ResponseDTO;
import uz.pdp.online_education.service.interfaces.ContactMessageService;

import java.util.List;

/**
 * ContactMessageController foydalanuvchilarning qo‘llab-quvvatlash (support) xabarlarini boshqarish uchun ishlatiladi.
 * Bu controller orqali foydalanuvchilar xabar yuborishi, admin/instructor esa ularga javob berishi mumkin.
 */
@RestController
@RequestMapping("/api/contact-messages")
@RequiredArgsConstructor
public class ContactMessageController {

    private final ContactMessageService contactMessageService;

    /**
     * Yangi xabar yuborish.
     * Bu endpoint orqali foydalanuvchi support teamga murojaat yuborishi mumkin.
     *
     * @param dto Xabar request DTO (ism, email, matn)
     * @return "Message sent successfully!" xabari
     */
    @Operation(summary = "Send a new contact message", description = "Allows a user to send a contact/support message.")
    @PostMapping
    public ResponseEntity<ResponseDTO<String>> create(@RequestBody ContactMessageRequestDTO dto) {
        contactMessageService.create(dto);
        return ResponseEntity.ok(ResponseDTO.success("Message sent successfully!"));
    }

    /**
     * Hali javob yozilmagan barcha xabarlarni olish.
     *
     * @return Javob yozilmagan xabarlar ro‘yxati
     */
    @Operation(summary = "Get all unreplied messages", description = "Fetches all messages that have not been replied to yet.")
    @GetMapping("/unreplied")
    public ResponseEntity<ResponseDTO<List<ContactMessageResponseDTO>>> getAllExceptReplied() {
        List<ContactMessageResponseDTO> result = contactMessageService.getAllExceptReplied();
        return ResponseEntity.ok(ResponseDTO.success(result));
    }

    /**
     * Barcha xabarlarni olish (javob berilgan va berilmagan).
     *
     * @return Barcha xabarlar
     */
    @Operation(summary = "Get all contact messages", description = "Fetches all contact messages (both replied and unreplied).")
    @GetMapping
    public ResponseEntity<ResponseDTO<List<ContactMessageResponseDTO>>> getAll() {
        List<ContactMessageResponseDTO> result = contactMessageService.getAll();
        return ResponseEntity.ok(ResponseDTO.success(result));
    }

    /**
     * Xabarni ID orqali olish va uni "read" sifatida belgilash.
     *
     * @param id Xabar IDsi
     * @return Shu IDga tegishli xabar
     */
    @Operation(summary = "Get message by ID", description = "Fetch a message by ID and mark it as read.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ContactMessageResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Message not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<ContactMessageResponseDTO>> getByIdAndMarkRead(
            @Parameter(description = "Message ID", example = "100") @PathVariable Long id) {
        ContactMessageResponseDTO dto = contactMessageService.getByIdAndMarkRead(id);
        return ResponseEntity.ok(ResponseDTO.success(dto));
    }

    /**
     * Admin yoki Instructor xabarga javob berishi mumkin.
     *
     * @param id        Xabar IDsi
     * @param replyText Javob matni
     * @return "Reply sent successfully!" xabari
     */
    @Operation(summary = "Reply to a message", description = "Allows an admin/instructor to reply to a contact message.")
    @PostMapping("/{id}/reply")
    public ResponseEntity<ResponseDTO<String>> replyToMessage(
            @Parameter(description = "Message ID", example = "100") @PathVariable Long id,
            @RequestBody String replyText) {
        contactMessageService.replyToMessage(id, replyText);
        return ResponseEntity.ok(ResponseDTO.success("Reply sent successfully!"));
    }

    /**
     * Foydalanuvchi o‘z yuborgan xabarini tahrirlashi mumkin,
     * lekin faqat unga javob berilmagan bo‘lsa.
     *
     * @param id  Xabar IDsi
     * @param dto Yangi xabar ma’lumotlari
     * @return "Message edited successfully" xabari
     */
    @Operation(summary = "Edit a contact message", description = "Allows a user to edit their message if it has not been replied to yet.")
    @PutMapping("/{id}")
    public ResponseEntity<ResponseDTO<String>> edit(
            @Parameter(description = "Message ID", example = "100") @PathVariable Long id,
            @RequestBody ContactMessageRequestDTO dto) {
        contactMessageService.edit(id, dto);
        return ResponseEntity.ok(ResponseDTO.success("Message edited successfully"));
    }
}
