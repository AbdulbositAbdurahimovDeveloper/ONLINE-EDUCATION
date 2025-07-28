package uz.pdp.online_education.payload.quiz;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.online_education.payload.content.ContentDTO;

import java.io.Serializable;

/**
 * DTO for {@link uz.pdp.online_education.model.lesson.QuizContent}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizContentResponseDTO implements Serializable , ContentDTO {
    private Long id;
    private Integer blockOrder;
    private String contentType;
    private Long lessonId;
    private QuizResponseDTO quiz;

}
