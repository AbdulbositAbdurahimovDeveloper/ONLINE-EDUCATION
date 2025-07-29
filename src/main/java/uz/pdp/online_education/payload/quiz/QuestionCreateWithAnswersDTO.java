package uz.pdp.online_education.payload.quiz;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.online_education.enums.QuestionType;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class QuestionCreateWithAnswersDTO implements Serializable {

    @NotBlank
    @Size(max = 2000)
    private String text;

    @NotNull
    private QuestionType type;

    @NotNull
    private Long quizId;

    @NotNull
    @Size(min = 2)
    List<AnswerOptionCreateNestedDTO> options;
}
