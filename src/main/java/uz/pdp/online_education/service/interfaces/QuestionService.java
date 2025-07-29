package uz.pdp.online_education.service.interfaces;

import uz.pdp.online_education.payload.quiz.QuestionCreateWithAnswersDTO;
import uz.pdp.online_education.payload.quiz.QuestionResponseDTO;
import uz.pdp.online_education.payload.quiz.QuestionUpdateDTO;

import java.util.List;

public interface QuestionService {

    QuestionResponseDTO createWithAnswers(QuestionCreateWithAnswersDTO createDTO);

    QuestionResponseDTO getById(Long id);

    List<QuestionResponseDTO> getAllByQuizId(Long quizId);

    QuestionResponseDTO update(Long id, QuestionUpdateDTO updateDTO);

    void delete(Long id);
}
