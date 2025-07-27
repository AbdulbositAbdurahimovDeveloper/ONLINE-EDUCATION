package uz.pdp.online_education.service.interfaces;

import uz.pdp.online_education.payload.quiz.AnswerOptionCreatDTO;
import uz.pdp.online_education.payload.quiz.AnswerOptionResponseDTO;
import uz.pdp.online_education.payload.quiz.AnswerOptionUpdateDTO;

public interface AnswerOptionService {
    AnswerOptionResponseDTO create(AnswerOptionCreatDTO createDTO);
    AnswerOptionResponseDTO update(Long optionId, AnswerOptionUpdateDTO updateDTO);
    void delete(Long optionId);
}
