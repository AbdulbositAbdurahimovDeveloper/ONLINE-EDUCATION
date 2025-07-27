package uz.pdp.online_education.payload.quiz;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.online_education.enums.QuestionType;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for {@link uz.pdp.online_education.model.quiz.Question}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionResponseDTO implements Serializable {

    private Long id;

    private String text;

    private QuestionType type;

    private List<AnswerOptionResponseDTO> options;
}
