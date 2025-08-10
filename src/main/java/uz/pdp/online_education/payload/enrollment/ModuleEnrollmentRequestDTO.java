package uz.pdp.online_education.payload.enrollment;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for creating a new module enrollment.
 * Used in the request body of the POST endpoint.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ModuleEnrollmentRequestDTO {

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotNull(message = "Module ID cannot be null")
    private Long moduleId;
}