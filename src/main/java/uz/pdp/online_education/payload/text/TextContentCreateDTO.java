package uz.pdp.online_education.payload.text;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link uz.pdp.online_education.model.lesson.TextContent}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TextContentCreateDTO implements Serializable {

    @NotNull
    private Long lessonId;

    @NotBlank
    private String text;
}