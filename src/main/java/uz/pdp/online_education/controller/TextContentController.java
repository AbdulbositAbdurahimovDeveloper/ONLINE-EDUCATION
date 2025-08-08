package uz.pdp.online_education.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.pdp.online_education.payload.ResponseDTO;
import uz.pdp.online_education.payload.text.TextContentCreateDTO;
import uz.pdp.online_education.payload.text.TextContentResponseDTO;
import uz.pdp.online_education.payload.text.TextContentUpdateDTO;
import uz.pdp.online_education.service.interfaces.TextContentService;

@RestController
@RequestMapping("/api/v1/text-content")
@RequiredArgsConstructor
public class TextContentController {

    private final TextContentService textContentService;

    @GetMapping("/{id}")
    @PreAuthorize(value = "hasRole('ADMIN') or hasRole('INSTRUCTOR') or @courseSecurity.isUserModuleBought(authentication, #id)")
    public ResponseEntity<ResponseDTO<?>> getTextContentById(@PathVariable("id") Long id) {
        TextContentResponseDTO textContentResponseDTO = textContentService.getById(id);
        return ResponseEntity.ok(ResponseDTO.success(textContentResponseDTO));
    }


    @PostMapping
    @PreAuthorize(value = "hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDTO<?>> createTextContent(TextContentCreateDTO textContentCreateDTO) {
        TextContentResponseDTO textContentResponseDTO = textContentService.create(textContentCreateDTO);
        return ResponseEntity.ok(ResponseDTO.success(textContentResponseDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize(value = "hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDTO<?>> updateTextContent(@PathVariable("id") Long id,
                                                            @RequestBody TextContentUpdateDTO textContentUpdateDTO) {
        TextContentResponseDTO textContentResponseDTO = textContentService.update(id, textContentUpdateDTO);
        return ResponseEntity.ok(ResponseDTO.success(textContentResponseDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(value = "hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDTO<?>> deleteTextContent(@PathVariable("id") Long id) {
        textContentService.delete(id);
        return ResponseEntity.ok(ResponseDTO.success("Text content deleted successfully"));
    }

}
