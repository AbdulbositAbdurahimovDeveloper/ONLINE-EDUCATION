package uz.pdp.online_education.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Attachment Content API", description = "Endpoints for managing lesson/course attachment contents like PDF, videos, etc.")
public class AttachmentContentController {

    private final AttachmentContentService attachmentContentService;

    @Operation(
            summary = "Get attachment content",
            description = "Fetches the attachment content (e.g., PDF, video, or file) by its ID. " +
                    "Accessible only to ADMIN, INSTRUCTOR, or users who purchased the related course/module."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attachment content retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AttachmentContentDTO.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "message": "Attachment content retrieved successfully",
                                      "data": {
                                        "id": 10,
                                        "fileName": "lesson1.pdf",
                                        "contentType": "application/pdf",
                                        "size": 524288
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "403", description = "You are not authorized to access this content",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "Access denied. You do not have permission."
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Attachment content not found with the given ID",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "Attachment content with ID=10 not found"
                                    }
                                    """)))
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

    @Operation(
            summary = "Create attachment content",
            description = "Creates and saves new attachment content (e.g., PDF, video, image). " +
                    "Only ADMIN and INSTRUCTOR roles can perform this operation."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attachment content created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AttachmentContentDTO.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "message": "Attachment content created successfully",
                                      "data": {
                                        "id": 5,
                                        "fileName": "intro.mp4",
                                        "contentType": "video/mp4",
                                        "size": 10485760
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "Validation failed. Required fields missing."
                                    }
                                    """))),
            @ApiResponse(responseCode = "403", description = "You are not authorized to perform this action")
    })
    @PostMapping
    @PreAuthorize(value = "hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDTO<?>> create(
            @RequestBody(
                    description = "Attachment content creation request",
                    required = true,
                    content = @Content(schema = @Schema(implementation = AttachmentContentCreateDTO.class))
            )
            @org.springframework.web.bind.annotation.RequestBody AttachmentContentCreateDTO attachmentContentCreateDTO
    ) {
        AttachmentContentDTO attachmentContentDTO = attachmentContentService.create(attachmentContentCreateDTO);
        return ResponseEntity.ok(ResponseDTO.success(attachmentContentDTO));
    }

    @Operation(
            summary = "Delete attachment content",
            description = "Deletes an existing attachment content by its ID. " +
                    "Only ADMIN and INSTRUCTOR roles are allowed to delete content."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attachment content deleted successfully",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "message": "Content deleted successfully"
                                    }
                                    """))),
            @ApiResponse(responseCode = "403", description = "You are not authorized to perform this action"),
            @ApiResponse(responseCode = "404", description = "Attachment content not found with the given ID")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize(value = "hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDTO<?>> delete(
            @Parameter(description = "ID of the attachment content to be deleted", example = "7")
            @PathVariable Long id
    ) {
        attachmentContentService.delete(id);
        return ResponseEntity.ok(ResponseDTO.success("Content deleted successfully"));
    }
}
