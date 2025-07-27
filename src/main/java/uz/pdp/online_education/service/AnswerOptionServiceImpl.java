package uz.pdp.online_education.service;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.mapper.AnswerOptionMapper;
import uz.pdp.online_education.model.quiz.AnswerOption;
import uz.pdp.online_education.model.quiz.Question;
import uz.pdp.online_education.payload.quiz.AnswerOptionCreatDTO;
import uz.pdp.online_education.payload.quiz.AnswerOptionResponseDTO;
import uz.pdp.online_education.payload.quiz.AnswerOptionUpdateDTO;
import uz.pdp.online_education.repository.AnswerOptionRepository;
import uz.pdp.online_education.repository.QuestionRepository;
import uz.pdp.online_education.service.interfaces.AnswerOptionService;

@Service
@RequestMapping
public class AnswerOptionServiceImpl implements AnswerOptionService {


    private final QuestionRepository questionRepository;
    private final AnswerOptionRepository answerOptionRepository;
    private final AnswerOptionMapper answerOptionMapper;

    public AnswerOptionServiceImpl(QuestionRepository questionRepository, AnswerOptionRepository answerOptionRepository, AnswerOptionMapper answerOptionMapper) {
        this.questionRepository = questionRepository;
        this.answerOptionRepository = answerOptionRepository;
        this.answerOptionMapper = answerOptionMapper;
    }

    @Override
    public AnswerOptionResponseDTO create(AnswerOptionCreatDTO createDTO) {

        Question question = questionRepository.findById(createDTO.getQuestionId())
                .orElseThrow(() -> new EntityNotFoundException("Question not found with id: " + createDTO.getQuestionId()));

        AnswerOption answerOption = new AnswerOption();
        answerOption.setText(createDTO.getText());
        answerOption.setCorrect(createDTO.isCorrect());

        question.addOption(answerOption);

        AnswerOption save = answerOptionRepository.save(answerOption);


        return answerOptionMapper.toDto(answerOption);
    }

    @Override
    public AnswerOptionResponseDTO update(Long optionId, AnswerOptionUpdateDTO updateDTO) {

        AnswerOption answerOption = answerOptionRepository.findById(optionId).orElseThrow(
                () -> new EntityNotFoundException("Answer option not found with id: " + optionId)
        );

        answerOption.setText(updateDTO.getText());
        answerOption.setCorrect(updateDTO.isCorrect());

        return answerOptionMapper.toDto(answerOptionRepository.save(answerOption));
    }

    @Override
    public void delete(Long optionId) {
        if (!answerOptionRepository.existsById(optionId)) {
            throw new EntityNotFoundException("Answer option not found with id: " + optionId);
        }
        answerOptionRepository.deleteById(optionId);
    }
}
