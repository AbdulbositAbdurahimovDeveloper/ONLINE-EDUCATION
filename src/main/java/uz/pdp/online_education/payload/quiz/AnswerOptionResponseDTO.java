package uz.pdp.online_education.payload.quiz;

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
public class AnswerOptionResponseDTO implements Serializable {
    private Long id;
    private String text;
    private boolean isCorrect;
}