package uz.pdp.online_education.service.interfaces;

import uz.pdp.online_education.payload.quiz.QuizContentCreateDTO;
import uz.pdp.online_education.payload.quiz.QuizContentResponseDTO;
import uz.pdp.online_education.payload.quiz.QuizResponseDTO;
import uz.pdp.online_education.payload.quiz.QuizUpdateDTO;

public interface QuizContentService {

    QuizContentResponseDTO getById(Long contentId);

    QuizContentResponseDTO create(QuizContentCreateDTO createDTO);

    QuizContentResponseDTO update(Long contentId, QuizUpdateDTO updateDTO);

    void delete(Long contentId);
}
