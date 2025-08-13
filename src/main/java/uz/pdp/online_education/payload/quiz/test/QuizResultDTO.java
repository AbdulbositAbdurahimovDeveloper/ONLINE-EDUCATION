package uz.pdp.online_education.payload.quiz.test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class QuizResultDTO {

    private Long attemptId;
    private String quizTitle;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int totalQuestions;
    private int score; // To'g'ri javoblar soni
    private double percentage;
    private List<UserAnswerResultDTO> answers; // Har bir savolga berilgan javob tahlili
}
