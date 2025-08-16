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
        public ResponseEntity<ResponseDTO<?>> getTextContentById(@PathVariable("id") Long id) {
            TextContentResponseDTO textContentResponseDTO = textContentService.getById(id);
            return ResponseEntity.ok(ResponseDTO.success(textContentResponseDTO));
        }

        @PostMapping
        @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
        @Operation(summary = "Create text content", description = "Create new text-based learning content")
        @ApiResponse(responseCode = "200", description = "Text content created successfully")
        public ResponseEntity<ResponseDTO<?>> createTextContent(@RequestBody TextContentCreateDTO textContentCreateDTO) {
            TextContentResponseDTO textContentResponseDTO = textContentService.create(textContentCreateDTO);
            return ResponseEntity.ok(ResponseDTO.success(textContentResponseDTO));
        }

        @PutMapping("/{id}")
        @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
        @Operation(summary = "Update text content", description = "Update existing text-based learning content by ID")
        @ApiResponse(responseCode = "200", description = "Text content updated successfully")
        public ResponseEntity<ResponseDTO<?>> updateTextContent(
                @PathVariable("id") Long id,
                @RequestBody TextContentUpdateDTO textContentUpdateDTO) {
            TextContentResponseDTO textContentResponseDTO = textContentService.update(id, textContentUpdateDTO);
            return ResponseEntity.ok(ResponseDTO.success(textContentResponseDTO));
        }

        @DeleteMapping("/{id}")
        @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
        @Operation(summary = "Delete text content", description = "Delete text-based learning content by ID")
        @ApiResponse(responseCode = "200", description = "Text content deleted successfully")
        public ResponseEntity<ResponseDTO<?>> deleteTextContent(@PathVariable("id") Long id) {
            textContentService.delete(id);
            return ResponseEntity.ok(ResponseDTO.success("Text content deleted successfully"));
        }
    }
