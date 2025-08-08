package uz.pdp.online_education.payload.quiz;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link uz.pdp.online_education.model.quiz.AnswerOption}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnswerOptionUpdateDTO implements Serializable {
    @Size(max = 100)
    @NotBlank
    private String text;

    @NotNull
    private boolean isCorrect;
}