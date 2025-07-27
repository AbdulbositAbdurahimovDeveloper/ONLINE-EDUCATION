package uz.pdp.online_education.payload.quiz;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link uz.pdp.online_education.model.lesson.QuizContent}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizContentCreateDTO implements Serializable {

    @NotNull
    private Long lessonId;

    @NotBlank
    @Size(min = 3, max = 50)
    private String quizTitle;

    private String quizDescription;
}