package uz.pdp.online_education.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.online_education.enums.QuestionType;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.mapper.AnswerOptionMapper;
import uz.pdp.online_education.model.quiz.AnswerOption;
import uz.pdp.online_education.model.quiz.Question;
import uz.pdp.online_education.payload.quiz.AnswerOptionResponseDTO;
import uz.pdp.online_education.repository.AnswerOptionRepository;
import uz.pdp.online_education.service.interfaces.AnswerOptionService;

@Service
@RequiredArgsConstructor
public class AnswerOptionServiceImpl implements AnswerOptionService {


    private final AnswerOptionRepository answerOptionRepository;
    private final AnswerOptionMapper answerOptionMapper;



    @Transactional
    @Override
    public void delete(Long optionId) {
        if (!answerOptionRepository.existsById(optionId)) {
            throw new EntityNotFoundException("Answer option not found with id: " + optionId);
        }
        answerOptionRepository.deleteById(optionId);
    }
}
