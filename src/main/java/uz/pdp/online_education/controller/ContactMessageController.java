    package uz.pdp.online_education.controller;

    import io.swagger.v3.oas.annotations.Operation;
    import io.swagger.v3.oas.annotations.Parameter;
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

        @Operation(summary = "Send a new contact message", description = "Allows a user to send a contact/support message.")
        @PostMapping
        public ResponseEntity<ResponseDTO<String>> create(@RequestBody ContactMessageRequestDTO dto) {
            contactMessageService.create(dto);
            return ResponseEntity.ok(ResponseDTO.success("Message sent successfully!"));
        }


        @Operation(summary = "Get all unreplied messages", description = "Fetches all messages that have not been replied to yet.")
        @GetMapping("/unreplied")
        public ResponseEntity<ResponseDTO<List<ContactMessageResponseDTO>>> getAllExceptReplied() {
            List<ContactMessageResponseDTO> result = contactMessageService.getAllExceptReplied();
            return ResponseEntity.ok(ResponseDTO.success(result));
        }


        @Operation(summary = "Get all contact messages", description = "Fetches all contact messages (replied and unreplied).")
        @GetMapping
        public ResponseEntity<ResponseDTO<List<ContactMessageResponseDTO>>> getAll() {
            List<ContactMessageResponseDTO> result = contactMessageService.getAll();
            return ResponseEntity.ok(ResponseDTO.success(result));
        }


        @Operation(summary = "Get message by ID", description = "Fetch a message by ID and mark it as read.")
        @GetMapping("/{id}")
        public ResponseEntity<ResponseDTO<ContactMessageResponseDTO>> getByIdAndMarkRead(
                @Parameter(description = "Message ID", example = "100") @PathVariable Long id) {
            ContactMessageResponseDTO dto = contactMessageService.getByIdAndMarkRead(id);
            return ResponseEntity.ok(ResponseDTO.success(dto));
        }


        @Operation(summary = "Reply to a message", description = "Allows an admin/instructor to reply to a contact message.")
        @PostMapping("/{id}/reply")
        public ResponseEntity<ResponseDTO<String>> replyToMessage(
                @Parameter(description = "Message ID", example = "100") @PathVariable Long id,
                @RequestBody String replyText) {
            contactMessageService.replyToMessage(id, replyText);
            return ResponseEntity.ok(ResponseDTO.success("Reply sent successfully!"));
        }


        @Operation(summary = "Edit a contact message", description = "Allows a user to edit their message if it has not been replied to yet.")
        @PutMapping("/{id}")
        public ResponseEntity<ResponseDTO<String>> edit(
                @Parameter(description = "Message ID", example = "100") @PathVariable Long id,
                @RequestBody ContactMessageRequestDTO dto) {
            contactMessageService.edit(id, dto);
            return ResponseEntity.ok(ResponseDTO.success("Message edited successfully"));
        }
    }
