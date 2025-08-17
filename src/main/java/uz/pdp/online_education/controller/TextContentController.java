package uz.pdp.online_education.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Text Content Controller", description = "APIs for managing text-based course content")
public class TextContentController {

    private final TextContentService textContentService;


    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR') or @courseSecurity.isUserModuleBought(authentication, #id)")
    @Operation(summary = "Get text content by ID", description = "Retrieve text content details by its ID")
    @ApiResponse(responseCode = "200", description = "Text content retrieved successfully")
    public ResponseEntity<ResponseDTO<TextContentResponseDTO>> getTextContentById(@PathVariable Long id) {
        TextContentResponseDTO response = textContentService.getById(id);
        return ResponseEntity.ok(ResponseDTO.success(response));
    }


    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @Operation(summary = "Create text content", description = "Create new text-based learning content")
    @ApiResponse(responseCode = "200", description = "Text content created successfully")
    public ResponseEntity<ResponseDTO<TextContentResponseDTO>> createTextContent(@RequestBody TextContentCreateDTO dto) {
        TextContentResponseDTO response = textContentService.create(dto);
        return ResponseEntity.ok(ResponseDTO.success(response));
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @Operation(summary = "Update text content", description = "Update existing text-based learning content by ID")
    @ApiResponse(responseCode = "200", description = "Text content updated successfully")
    public ResponseEntity<ResponseDTO<TextContentResponseDTO>> updateTextContent(
            @PathVariable Long id,
            @RequestBody TextContentUpdateDTO dto) {
        TextContentResponseDTO response = textContentService.update(id, dto);
        return ResponseEntity.ok(ResponseDTO.success(response));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @Operation(summary = "Delete text content", description = "Delete text-based learning content by ID")
    @ApiResponse(responseCode = "200", description = "Text content deleted successfully")
    public ResponseEntity<ResponseDTO<String>> deleteTextContent(@PathVariable Long id) {
        textContentService.delete(id);
        return ResponseEntity.ok(ResponseDTO.success("Text content deleted successfully"));
    }

}
