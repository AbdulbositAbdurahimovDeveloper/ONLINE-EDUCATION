package uz.pdp.online_education.payload.module;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for creating a new Module.
 * This class includes validation constraints for the incoming data.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModuleCreateDTO {

    @NotBlank(message = "Module title cannot be blank.")
    @Size(min = 3, max = 200, message = "Title must be between {min} and {max} characters.")
    private String title;

    @Size(max = 1000, message = "Description must not exceed {max} characters.")
    private String description;

    @NotNull(message = "Module price cannot be null.")
    @Min(value = 0, message = "Price cannot be negative.")
    private Long price;

    @NotNull(message = "Course ID cannot be null.")
    private Long courseId;
}