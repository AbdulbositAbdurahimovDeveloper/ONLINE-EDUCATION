package uz.pdp.online_education.payload.enrollment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for returning module enrollment information in API responses.
 * Provides a client-friendly view of an enrollment.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ModuleEnrollmentResponseDTO {

    private Long id; // The ID of the enrollment itself

    // --- User Information ---
    private Long userId;
    private String userFullName; // More useful than just the ID

    // --- Module Information ---
    private Long moduleId;
    private String moduleTitle; // More useful than just the ID
    
    // --- Course Information (for context) ---
    private Long courseId;
    private String courseTitle;

    // --- Progress Information ---
    private double progressPercentage;
}