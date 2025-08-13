package uz.pdp.online_education.payload.quiz.test;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AnswerSubmissionDTO {

    @NotNull
    private Long questionId;

    private Set<Long> selectedOptionIds;
}
