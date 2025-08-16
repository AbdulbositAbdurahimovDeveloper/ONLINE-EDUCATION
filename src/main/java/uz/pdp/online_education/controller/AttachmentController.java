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
@Tag(name = "Attachment API", description = "Endpoints for uploading, downloading, and managing attachments (files, icons, links)")
public class AttachmentController {

    private final AttachmentService attachmentService;

    @Operation(
            summary = "Get attachment by ID",
            description = "Fetches metadata of an attachment (e.g., file name, size, type) by its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attachment retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AttachmentDTO.class))),
            @ApiResponse(responseCode = "404", description = "Attachment not found with the given ID")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<AttachmentDTO>> read(
            @Parameter(description = "ID of the attachment", example = "12")
            @PathVariable Long id
    ) {
        AttachmentDTO attachmentDTO = attachmentService.read(id);
        return ResponseEntity.ok(ResponseDTO.success(attachmentDTO));
    }

    @Operation(
            summary = "Upload attachment",
            description = "Uploads a new attachment file (e.g., PDF, image, video) to the server.",
            requestBody = @RequestBody(
                    description = "Multipart file to be uploaded",
                    required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary"))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attachment uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file upload request")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDTO<AttachmentDTO>> create(
            @RequestPart("file") MultipartFile multipartFile
    ) {
        AttachmentDTO attachmentDTO = attachmentService.create(multipartFile);
        return ResponseEntity.ok(ResponseDTO.success(attachmentDTO));
    }

    @Operation(
            summary = "Upload an icon",
            description = "Uploads an icon (e.g., image used as course or lesson icon).",
            requestBody = @RequestBody(
                    description = "Multipart icon file",
                    required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary"))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Icon uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid icon file")
    })
    @PostMapping(value = "/icons", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDTO<?>> saveIcon(
            @RequestPart("file") MultipartFile multipartFile
    ) {
        AttachmentDTO attachmentDTO = attachmentService.saveIcon(multipartFile);
        return ResponseEntity.ok(ResponseDTO.success(attachmentDTO));
    }

    @Operation(
            summary = "Get icon file",
            description = "Downloads the requested icon file by filename."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Icon file retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Icon file not found")
    })
    @GetMapping("/open/file/icons/{filename:.+}")
    public ResponseEntity<Resource> getIcon(
            @Parameter(description = "Filename of the icon (with extension)", example = "logo.png")
            @PathVariable String filename,
            HttpServletRequest request
    ) {
        return null; // service layerda implement qilinadi
    }

    @Operation(
            summary = "Generate temporary link",
            description = "Generates a temporary signed URL for accessing the attachment file. " +
                    "The link will expire after the specified number of minutes."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Temporary link generated successfully",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    "https://cdn.example.com/temp/abc123?expires=2025-08-16T15:30:00Z"
                                    """))),
            @ApiResponse(responseCode = "404", description = "Attachment not found with the given ID")
    })
    @GetMapping("/temp-link/{id}")
    public ResponseEntity<?> tempLink(
            @Parameter(description = "ID of the attachment", example = "15")
            @PathVariable Long id,
            @Parameter(description = "Link expiration time in minutes", example = "5")
            @RequestParam(defaultValue = "1") Integer minute
    ) {
        String presignedObjectUrl = attachmentService.tempLink(id, minute);
        return new ResponseEntity<>(presignedObjectUrl, HttpStatus.OK);
    }

    @Operation(
            summary = "Delete attachment",
            description = "Deletes an attachment from the system by its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attachment deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Attachment not found with the given ID")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDTO<String>> delete(
            @Parameter(description = "ID of the attachment to delete", example = "21")
            @PathVariable Long id
    ) {
        attachmentService.delete(id);
        return ResponseEntity.ok(ResponseDTO.success("Attachment deleted"));
    }
}
