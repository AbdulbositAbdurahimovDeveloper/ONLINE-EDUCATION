package uz.pdp.online_education.service.interfaces;

import uz.pdp.online_education.payload.quiz.QuestionCreateWithAnswersDTO;
import uz.pdp.online_education.payload.quiz.QuestionResponseDTO;
import uz.pdp.online_education.payload.quiz.QuestionUpdateWithAnswersDTO;

import java.util.List;

public interface QuestionService {

    QuestionResponseDTO createWithAnswers(QuestionCreateWithAnswersDTO createDTO);

    QuestionResponseDTO getById(Long id);

    List<QuestionResponseDTO> getAllByQuizId(Long quizId);

    QuestionResponseDTO updateWithAnswers(Long questionId, QuestionUpdateWithAnswersDTO updateDTO);

    void delete(Long id);

    boolean isUserQuestionBought(String username, Long quizId);
}
