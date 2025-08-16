package uz.pdp.online_education.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.pdp.online_education.payload.ModuleOrderIndexDTO;
import uz.pdp.online_education.payload.PageDTO;
import uz.pdp.online_education.payload.ResponseDTO;
import uz.pdp.online_education.payload.lesson.LessonResponseDTO;
import uz.pdp.online_education.payload.module.ModuleCreateDTO;
import uz.pdp.online_education.payload.module.ModuleDetailDTO;
import uz.pdp.online_education.payload.module.ModuleUpdateDTO;
import uz.pdp.online_education.payload.payment.PaymentDTO;
import uz.pdp.online_education.service.interfaces.ModuleService;
import uz.pdp.online_education.service.interfaces.PaymentService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/modules")
@RequiredArgsConstructor
@Tag(
        name = "Modules",
        description = "Endpoints for managing course modules"
)
public class ModuleController {

    private final ModuleService moduleService;
    private final PaymentService paymentService;

    @GetMapping("/{id}")
    @PreAuthorize(value = "hasRole('ADMIN') or hasRole('INSTRUCTOR') or @courseSecurity.isUserEnrolled(authentication, #id)")
    @Operation(
            summary = "Get module by ID",
            description = "Returns module details if the user has access (admin, instructor or enrolled student).",
            parameters = @Parameter(name = "id", description = "Module ID", example = "12"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Module successfully retrieved",
                            content = @Content(schema = @Schema(implementation = ModuleDetailDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Module not found")
            }
    )
    public ResponseEntity<ResponseDTO<ModuleDetailDTO>> read(@PathVariable Long id) {
        ModuleDetailDTO moduleDetailDTO = moduleService.read(id);
        return ResponseEntity.ok(ResponseDTO.success(moduleDetailDTO));
    }

    @PostMapping
    @PreAuthorize(value = "hasRole('ADMIN') or hasRole('INSTRUCTOR')")
    @Operation(
            summary = "Create module",
            description = "Creates a new module inside a course",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Module successfully created")
            }
    )
    public ResponseEntity<ResponseDTO<ModuleDetailDTO>> create(@RequestBody ModuleCreateDTO moduleCreateDTO) {
        ModuleDetailDTO moduleDetailDTO = moduleService.create(moduleCreateDTO);
        return ResponseEntity.ok(ResponseDTO.success(moduleDetailDTO));
    }

    @GetMapping("/{id}/lessons")
    @PreAuthorize(value = "hasRole('ADMIN') or hasRole('INSTRUCTOR') or @courseSecurity.isUserEnrolled(authentication, #id)")
    @Operation(
            summary = "Get lessons of module",
            description = "Returns paginated list of lessons inside a module.",
            parameters = {
                    @Parameter(name = "id", description = "Module ID", example = "12"),
                    @Parameter(name = "page", description = "Page number (default 0)", example = "0"),
                    @Parameter(name = "size", description = "Page size (default 10)", example = "10")
            }
    )
    public ResponseEntity<ResponseDTO<?>> readLessons(@PathVariable Long id,
                                                      @RequestParam(defaultValue = "0") Integer page,
                                                      @RequestParam(defaultValue = "10") Integer size) {
        PageDTO<LessonResponseDTO> lessonResponseDTO = moduleService.readLessons(id,page,size);
        return ResponseEntity.ok(ResponseDTO.success(lessonResponseDTO));
    }

    @GetMapping("/{id}/payment")
    @PreAuthorize(value = "hasRole('ADMIN') or hasRole('INSTRUCTOR')")
    @Operation(
            summary = "Get payments of module",
            description = "Returns paginated list of payments for the given module."
    )
    public ResponseEntity<ResponseDTO<?>> readPayments(@PathVariable Long id,
                                                       @RequestParam(defaultValue = "0") Integer page,
                                                       @RequestParam(defaultValue = "10") Integer size) {
        PageDTO<PaymentDTO> paymentDTO = paymentService.readPayments(id,page,size);
        return  ResponseEntity.ok(ResponseDTO.success(paymentDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize(value = "hasRole('ADMIN') or hasRole('INSTRUCTOR')")
    @Operation(
            summary = "Update module",
            description = "Updates module information by ID"
    )
    public ResponseEntity<ResponseDTO<ModuleDetailDTO>> update(@PathVariable Long id, @RequestBody ModuleUpdateDTO moduleUpdateDTO) {
        ModuleDetailDTO moduleDetailDTO = moduleService.update(id, moduleUpdateDTO);
        return ResponseEntity.ok(ResponseDTO.success(moduleDetailDTO));
    }

    @PatchMapping("/{courseId}/reorder")
    @PreAuthorize(value = "hasRole('ADMIN') or hasRole('INSTRUCTOR')")
    @Operation(
            summary = "Reorder modules",
            description = "Updates the order of modules inside a course."
    )
    public ResponseEntity<ResponseDTO<?>> reorder(@PathVariable Long courseId, @RequestBody List<ModuleOrderIndexDTO> moduleOrderDTOS) {
        moduleService.updateModuleOrderIndex(courseId, moduleOrderDTOS);
        return ResponseEntity.ok(ResponseDTO.success("successful"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(value = "hasRole('ADMIN')")
    @Operation(
            summary = "Delete module",
            description = "Deletes module by ID"
    )
    public ResponseEntity<ResponseDTO<String>> delete(@PathVariable Long id) {
        moduleService.delete(id);
        return ResponseEntity.ok(ResponseDTO.success("Module deleted"));
    }
}
