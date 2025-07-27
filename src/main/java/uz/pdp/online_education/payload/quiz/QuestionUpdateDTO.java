package uz.pdp.online_education.payload.quiz;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.online_education.enums.QuestionType;

import java.io.Serializable;

/**
 * DTO for {@link uz.pdp.online_education.model.quiz.Question}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionUpdateDTO implements Serializable {

    @Size(min = 3, max = 50)
    @NotBlank
    private String text;

    @NotNull
    private QuestionType type;
}