package uz.pdp.online_education.payload.quiz;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for {@link uz.pdp.online_education.model.quiz.Quiz}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizResponseDTO implements Serializable {
    private Long id;
    private String title;
    private String description;
    private List<QuestionResponseDTO> questions;
}
