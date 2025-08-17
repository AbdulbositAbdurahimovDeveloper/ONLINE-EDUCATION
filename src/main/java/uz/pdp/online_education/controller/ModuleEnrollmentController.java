package uz.pdp.online_education.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.online_education.payload.ResponseDTO;
import uz.pdp.online_education.payload.enrollment.ModuleEnrollmentRequestDTO;
import uz.pdp.online_education.payload.enrollment.ModuleEnrollmentResponseDTO;
import uz.pdp.online_education.service.interfaces.ModuleEnrollmentService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/module-enrollment")
@RequiredArgsConstructor
@Tag(name = "Module Enrollment", description = "APIs to manage module enrollments")
public class ModuleEnrollmentController {

    private final ModuleEnrollmentService enrollmentService;

    @PostMapping("/enroll")
    @Operation(summary = "Enroll user in a module",
            description = "Enrolls a user in a specific module",
            responses = @ApiResponse(responseCode = "201", description = "User successfully enrolled"))
    public ResponseEntity<ResponseDTO<ModuleEnrollmentResponseDTO>> enrollUser(
            @Valid @RequestBody ModuleEnrollmentRequestDTO requestDTO) {
        ModuleEnrollmentResponseDTO newEnrollment = enrollmentService.enrollUser(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDTO.success(newEnrollment));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all enrollments of a user",
            description = "Retrieves all module enrollments for a specific user",
            responses = @ApiResponse(responseCode = "200", description = "Enrollments retrieved successfully"))
    public ResponseEntity<ResponseDTO<List<ModuleEnrollmentResponseDTO>>> getEnrollmentsByUser(
            @PathVariable Long userId) {
        List<ModuleEnrollmentResponseDTO> enrollments = enrollmentService.getEnrollmentsByUser(userId);
        return ResponseEntity.ok(ResponseDTO.success(enrollments));
    }

    @GetMapping("/module/{moduleId}")
    @Operation(summary = "Get all enrollments for a module",
            description = "Retrieves all enrollments for a specific module",
            responses = @ApiResponse(responseCode = "200", description = "Enrollments retrieved successfully"))
    public ResponseEntity<ResponseDTO<List<ModuleEnrollmentResponseDTO>>> getEnrollmentsByModule(
            @PathVariable Long moduleId) {
        List<ModuleEnrollmentResponseDTO> enrollments = enrollmentService.getEnrollmentsByModule(moduleId);
        return ResponseEntity.ok(ResponseDTO.success(enrollments));
    }

    @DeleteMapping("/{enrollmentId}")
    @Operation(summary = "Unenroll user from a module",
            description = "Removes enrollment by enrollment ID",
            responses = @ApiResponse(responseCode = "200", description = "Enrollment deleted successfully"))
    public ResponseEntity<ResponseDTO<String>> unenrollUser(@PathVariable Long enrollmentId) {
        enrollmentService.unenrollUser(enrollmentId);
        return ResponseEntity.ok(ResponseDTO.success("Enrollment with ID " + enrollmentId + " has been successfully deleted."));
    }
}
