package uz.pdp.online_education.payload.quiz;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.online_education.enums.QuestionType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for {@link uz.pdp.online_education.model.lesson.QuizContent}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizContentResponseDTO implements Serializable {
    private Long id;
    private Integer blockOrder;
    private String contentType;
    private Long lessonId;
    private QuizResponseDTO quiz;

}
