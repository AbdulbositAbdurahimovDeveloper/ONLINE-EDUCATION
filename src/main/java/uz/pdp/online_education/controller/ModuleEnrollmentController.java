    package uz.pdp.online_education.controller;

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

    /**
     * REST controller for managing module enrollments.
     */
    @RestController
    @RequestMapping("/api/v1/module-enrollment")
    @RequiredArgsConstructor
    public class ModuleEnrollmentController {

        private final ModuleEnrollmentService enrollmentService;

        /**
         * Enrolls a user in a specific module.
         *
         * @param requestDTO DTO containing userId and moduleId.
         * @return A ResponseDTO containing the newly created enrollment details.
         */
        @PostMapping("/enroll")
        public ResponseEntity<ResponseDTO<ModuleEnrollmentResponseDTO>> enrollUser(
                @Valid @RequestBody ModuleEnrollmentRequestDTO requestDTO) {
            ModuleEnrollmentResponseDTO newEnrollment = enrollmentService.enrollUser(requestDTO);
            return ResponseEntity
                    .status(HttpStatus.CREATED) // Use 201 Created for new resource creation
                    .body(ResponseDTO.success(newEnrollment));
        }

        /**
         * Retrieves all enrollments for a specific user.
         *
         * @param userId The ID of the user.
         * @return A list of enrollments for the user.
         */
        @GetMapping("/user/{userId}")
        public ResponseEntity<ResponseDTO<List<ModuleEnrollmentResponseDTO>>> getEnrollmentsByUser(
                @PathVariable Long userId) {
            List<ModuleEnrollmentResponseDTO> enrollments = enrollmentService.getEnrollmentsByUser(userId);
            return ResponseEntity.ok(ResponseDTO.success(enrollments));
        }

        /**
         * Retrieves all enrollments for a specific module.
         *
         * @param moduleId The ID of the module.
         * @return A list of enrollments for the module.
         */
        @GetMapping("/module/{moduleId}")
        public ResponseEntity<ResponseDTO<List<ModuleEnrollmentResponseDTO>>> getEnrollmentsByModule(
                @PathVariable Long moduleId) {
            List<ModuleEnrollmentResponseDTO> enrollments = enrollmentService.getEnrollmentsByModule(moduleId);
            return ResponseEntity.ok(ResponseDTO.success(enrollments));
        }

        /**
         * Unenrolls a user from a module by deleting the enrollment record.
         *
         * @param enrollmentId The ID of the enrollment to delete.
         * @return A success message.
         */
        @DeleteMapping("/{enrollmentId}")
        public ResponseEntity<ResponseDTO<String>> unenrollUser(@PathVariable Long enrollmentId) {
            enrollmentService.unenrollUser(enrollmentId);
            return ResponseEntity.ok(ResponseDTO.success("Enrollment with ID " + enrollmentId + " has been successfully deleted."));
        }
    }