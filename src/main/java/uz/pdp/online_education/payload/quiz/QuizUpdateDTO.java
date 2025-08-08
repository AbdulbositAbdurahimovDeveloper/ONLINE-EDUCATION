package uz.pdp.online_education.payload.quiz;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link uz.pdp.online_education.model.quiz.Quiz}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizUpdateDTO implements Serializable {

    @NotBlank
    @Size(min = 3, max = 50)
    private String title;

    private String description;
}