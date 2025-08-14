package uz.pdp.online_education.payload.quiz.test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.online_education.payload.quiz.QuestionResponseDTO;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class StartAttemptResponseDTO {

    private Long attemptId;

    private QuestionResponseDTO firstQuestion;
}
