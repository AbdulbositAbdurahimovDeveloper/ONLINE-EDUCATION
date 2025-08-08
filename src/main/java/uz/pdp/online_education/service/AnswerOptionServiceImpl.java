package uz.pdp.online_education.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import uz.pdp.online_education.enums.QuestionType;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.mapper.AnswerOptionMapper;
import uz.pdp.online_education.model.quiz.AnswerOption;
import uz.pdp.online_education.model.quiz.Question;
import uz.pdp.online_education.payload.quiz.AnswerOptionResponseDTO;
import uz.pdp.online_education.payload.quiz.AnswerOptionUpdateDTO;
import uz.pdp.online_education.repository.AnswerOptionRepository;
import uz.pdp.online_education.repository.QuestionRepository;
import uz.pdp.online_education.service.interfaces.AnswerOptionService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnswerOptionServiceImpl implements AnswerOptionService {


    private final AnswerOptionRepository answerOptionRepository;
    private final AnswerOptionMapper answerOptionMapper;



    @Transactional
    @Override
    public AnswerOptionResponseDTO update(Long optionId, AnswerOptionUpdateDTO updateDTO) {

        AnswerOption answerOption = answerOptionRepository.findById(optionId)
                .orElseThrow(() -> new EntityNotFoundException("Answer option not found with id: " + optionId));

        Question question = answerOption.getQuestion();


        if (question.getType() == QuestionType.SINGLE_CHOICE && updateDTO.isCorrect()) {

            answerOptionRepository.setAllOtherOptionsAsIncorrect(question.getId(), optionId);
        }

        answerOption.setText(updateDTO.getText());
        answerOption.setCorrect(updateDTO.isCorrect());


        AnswerOption savedOption = answerOptionRepository.save(answerOption);


        return answerOptionMapper.toDto(savedOption);
    }

    @Transactional
    @Override
    public void delete(Long optionId) {
        if (!answerOptionRepository.existsById(optionId)) {
            throw new EntityNotFoundException("Answer option not found with id: " + optionId);
        }
        answerOptionRepository.deleteById(optionId);
    }
}
