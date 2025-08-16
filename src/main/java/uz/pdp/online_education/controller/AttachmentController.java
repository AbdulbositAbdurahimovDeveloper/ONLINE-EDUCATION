package uz.pdp.online_education.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uz.pdp.online_education.payload.content.attachmentContent.AttachmentDTO;
import uz.pdp.online_education.payload.ResponseDTO;
import uz.pdp.online_education.service.interfaces.AttachmentService;

@RestController
@RequestMapping("/api/attachment")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<AttachmentDTO>> read(@PathVariable Long id) {
        AttachmentDTO attachmentDTO = attachmentService.read(id);
        return ResponseEntity.ok(ResponseDTO.success(attachmentDTO));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDTO<AttachmentDTO>> create(@RequestPart("file") MultipartFile multipartFile) {
        AttachmentDTO attachmentDTO = attachmentService.create(multipartFile);
        return ResponseEntity.ok(ResponseDTO.success(attachmentDTO));
    }

    @PostMapping(value = "/icons", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDTO<?>> saveIcon(@RequestPart("file") MultipartFile multipartFile) {
        AttachmentDTO attachmentDTO = attachmentService.saveIcon(multipartFile);
        return ResponseEntity.ok(ResponseDTO.success(attachmentDTO));
    }

    @GetMapping("/open/file/icons/{filename:.+}")
    public ResponseEntity<Resource> getIcon(@PathVariable String filename, HttpServletRequest request) {

        return null;
    }

    @GetMapping("/temp-link/{id}")
    public ResponseEntity<?> tempLink(@PathVariable Long id,
                                      @RequestParam(defaultValue = "1") Integer minute) {
        String presignedObjectUrl = attachmentService.tempLink(id, minute);
        return new ResponseEntity<>(presignedObjectUrl, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDTO<String>> delete(@PathVariable Long id) {
        attachmentService.delete(id);
        return ResponseEntity.ok(ResponseDTO.success("Attachment deleted"));
    }
}
