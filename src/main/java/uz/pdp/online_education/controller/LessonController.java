package uz.pdp.online_education.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.pdp.online_education.payload.ResponseDTO;
import uz.pdp.online_education.payload.lesson.*;
import uz.pdp.online_education.service.interfaces.LessonService;

@RestController
@RequestMapping("/api/v1/lessons")
@RequiredArgsConstructor
@Tag(name = "Lessons", description = "Endpoints for managing lessons inside modules")
public class LessonController {

    private final LessonService lessonService;

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR') or @courseSecurity.isPaymentOrFreeLesson(authentication, #id)")
    @Operation(
            summary = "Get lesson by ID",
            description = "Returns a lesson if the user is authorized (admin, instructor, or student with access).",
            parameters = @Parameter(name = "id", description = "Lesson ID", example = "101"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lesson successfully retrieved",
                            content = @Content(schema = @Schema(implementation = LessonResponseDTO.class))),
                    @ApiResponse(responseCode = "403", description = "Access denied"),
                    @ApiResponse(responseCode = "404", description = "Lesson not found")
            }
    )
    public ResponseEntity<ResponseDTO<?>> read(@PathVariable("id") Long id) {
        LessonResponseDTO lessonResponseDTO = lessonService.read(id);
        return ResponseEntity.ok(ResponseDTO.success(lessonResponseDTO));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @Operation(
            summary = "Create a new lesson",
            description = "Adds a new lesson to the selected module",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Lesson creation payload",
                    required = true,
                    content = @Content(schema = @Schema(implementation = LessonCreatDTO.class),
                            examples = @ExampleObject(value = "{ \"title\": \"Introduction to Java\", \"moduleId\": 5 }"))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lesson successfully created"),
                    @ApiResponse(responseCode = "400", description = "Invalid lesson data")
            }
    )
    public ResponseEntity<ResponseDTO<?>> create(@RequestBody @Valid LessonCreatDTO lessonCreatDTO) {
        LessonResponseDTO lessonResponseDTO = lessonService.create(lessonCreatDTO);
        return ResponseEntity.ok(ResponseDTO.success(lessonResponseDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @Operation(
            summary = "Update lesson",
            description = "Updates lesson data by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lesson successfully updated"),
                    @ApiResponse(responseCode = "404", description = "Lesson not found")
            }
    )
    public ResponseEntity<ResponseDTO<?>> update(@PathVariable("id") Long id,
                                                 @RequestBody @Valid LessonUpdateDTO lessonUpdateDTO) {
        LessonResponseDTO lessonResponseDTO = lessonService.update(id, lessonUpdateDTO);
        return ResponseEntity.ok(ResponseDTO.success(lessonResponseDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @Operation(
            summary = "Delete lesson",
            description = "Deletes lesson by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lesson successfully deleted"),
                    @ApiResponse(responseCode = "404", description = "Lesson not found")
            }
    )
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        lessonService.delete(id);
        return ResponseEntity.ok(ResponseDTO.success("Lesson deleted successfully"));
    }

    @PatchMapping("/order/{moduleId}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @Operation(
            summary = "Reorder lessons",
            description = "Updates the order of lessons inside a module",
            parameters = @Parameter(name = "moduleId", description = "Module ID where lessons belong", example = "5"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lesson order updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid order request")
            }
    )
    public ResponseEntity<ResponseDTO<?>> updateLessonOrder(
            @PathVariable Long moduleId,
            @Valid @RequestBody LessonOrderUpdateDTO request) {
        lessonService.updateOrder(moduleId, request.getOrderedIds());
        return ResponseEntity.ok(ResponseDTO.success("Lesson order updated successfully"));
    }
}
