package uz.pdp.online_education.payload.quiz.test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.online_education.payload.quiz.AnswerOptionResponseDTO;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserAnswerResultDTO {

    private Long questionId;
    private String questionText;
    private Set<AnswerOptionResponseDTO> selectedOptions; // Foydalanuvchi tanlagan javoblar
    private Set<AnswerOptionResponseDTO> correctOptions;  // Aslida to'g'ri bo'lgan javoblar
    private boolean wasCorrect; // Foydalanuvchining javobi to'g'ri edimi?
}
