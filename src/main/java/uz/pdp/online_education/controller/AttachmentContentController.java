package uz.pdp.online_education.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    /**
     * Retrieve (download) attachment content by ID.
     * Accessible to ADMIN, INSTRUCTOR or students who purchased the module.
     */
    @Operation(
            summary = "Get attachment content",
            description = "Fetches the attachment content (e.g., PDF, video, or file) by its ID. " +
                    "Accessible only to ADMIN, INSTRUCTOR, or users who purchased the related course/module."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attachment content retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "You are not authorized to access this content"),
            @ApiResponse(responseCode = "404", description = "Attachment content not found with the given ID")
    })
    @GetMapping("/{id}")
    @PreAuthorize(value = "hasRole('ADMIN') or hasRole('INSTRUCTOR') or @courseSecurity.isUserModuleBought(authentication, #id)")
    public ResponseEntity<ResponseDTO<AttachmentContentDTO>> uploadFile(
            @Parameter(description = "ID of the attachment content", example = "10")
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        AttachmentContentDTO attachmentContentDTO = attachmentContentService.uploadFile(id, currentUser);
        return ResponseEntity.ok(ResponseDTO.success(attachmentContentDTO));
    }

    /**
     * Create a new attachment content (e.g., upload a file).
     * Accessible only to ADMIN and INSTRUCTOR roles.
     */
    @Operation(
            summary = "Create attachment content",
            description = "Creates and saves new attachment content (e.g., PDF, video, image). " +
                    "Only ADMIN and INSTRUCTOR roles can perform this operation."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attachment content created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "You are not authorized to perform this action")
    })
    @PostMapping
    @PreAuthorize(value = "hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDTO<?>> create(
            @RequestBody AttachmentContentCreateDTO attachmentContentCreateDTO
    ) {
        AttachmentContentDTO attachmentContentDTO = attachmentContentService.create(attachmentContentCreateDTO);
        return ResponseEntity.ok(ResponseDTO.success(attachmentContentDTO));
    }

    /**
     * Delete an attachment content by ID.
     * Accessible only to ADMIN and INSTRUCTOR roles.
     */
    @Operation(
            summary = "Delete attachment content",
            description = "Deletes an existing attachment content by its ID. " +
                    "Only ADMIN and INSTRUCTOR roles are allowed to delete content."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attachment content deleted successfully"),
            @ApiResponse(responseCode = "403", description = "You are not authorized to perform this action"),
            @ApiResponse(responseCode = "404", description = "Attachment content not found with the given ID")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize(value = "hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDTO<?>> delete(
            @Parameter(description = "ID of the attachment content to be deleted", example = "7")
            @PathVariable Long id
    )
    {
        attachmentContentService.delete(id);
        return ResponseEntity.ok(ResponseDTO.success("Content deleted successfully"));
    }
}
