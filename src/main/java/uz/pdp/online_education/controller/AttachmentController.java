package uz.pdp.online_education.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import uz.pdp.online_education.payload.AttachmentDTO;
import uz.pdp.online_education.payload.ResponseDTO;
import uz.pdp.online_education.service.AttachmentService;

import java.io.IOException;

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

    @PostMapping
    public ResponseEntity<ResponseDTO<AttachmentDTO>> create(@RequestPart("file") MultipartFile multipartFile) {
        AttachmentDTO attachmentDTO = attachmentService.create(multipartFile);
        return ResponseEntity.ok(ResponseDTO.success(attachmentDTO));
    }

    @PostMapping("/icons")
    public ResponseEntity<ResponseDTO<?>> saveIcon(@RequestPart("file") MultipartFile multipartFile) {
        AttachmentDTO attachmentDTO = attachmentService.saveIcon(multipartFile);
        return ResponseEntity.ok(ResponseDTO.success(attachmentDTO));
    }

    @GetMapping("/open/file/icons/{filename:.+}")
    public ResponseEntity<Resource> getIcon(@PathVariable String filename, HttpServletRequest request) {
        Resource resource = attachmentService.loadIconAsResource(filename);

        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            // log error
        }

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"") // inline -> brauzerda ochadi
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDTO<String>> delete(@PathVariable Long id) {
        attachmentService.delete(id);
        return ResponseEntity.ok(ResponseDTO.success("Attachment deleted"));
    }
}
