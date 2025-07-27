package uz.pdp.online_education.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.payload.ResponseDTO;
import uz.pdp.online_education.payload.content.attachmentContent.AttachmentContentCreateDTO;
import uz.pdp.online_education.payload.lesson.AttachmentContentDTO;
import uz.pdp.online_education.service.interfaces.AttachmentContentService;

@RestController
@RequestMapping("/api/v1/attachment-content")
@RequiredArgsConstructor
public class AttachmentContentController {

    private final AttachmentContentService attachmentContentService;


    @PreAuthorize(value = "hasRole('ADMIN') or hasRole('INSTRUCTOR') or @courseSecurity.isUserModuleBought(authentication, #id)")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<AttachmentContentDTO>> uploadFile(@PathVariable Long id,
                                                                        @AuthenticationPrincipal User currentUser) {
        AttachmentContentDTO attachmentContentDTO = attachmentContentService.uploadFile(id,currentUser);
        return ResponseEntity.ok(ResponseDTO.success(attachmentContentDTO));
    }

    @PostMapping
    public ResponseEntity<ResponseDTO<?>> create(@RequestBody AttachmentContentCreateDTO attachmentContentCreateDTO){
        AttachmentContentDTO attachmentContentDTO = attachmentContentService.create(attachmentContentCreateDTO);
        return ResponseEntity.ok(ResponseDTO.success(attachmentContentDTO));
    }

    @DeleteMapping("/{id}")
    public  ResponseEntity<ResponseDTO<?>> delete(@PathVariable Long id) {
        attachmentContentService.delete(id);
        return  ResponseEntity.ok(ResponseDTO.success("content deleted"));
    }
}
