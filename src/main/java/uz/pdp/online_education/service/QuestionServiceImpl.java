package uz.pdp.online_education.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.mapper.QuestionMapper;
import uz.pdp.online_education.model.quiz.Question;
import uz.pdp.online_education.model.quiz.Quiz;
import uz.pdp.online_education.payload.quiz.QuestionCreateDTO;
import uz.pdp.online_education.payload.quiz.QuestionResponseDTO;
import uz.pdp.online_education.payload.quiz.QuestionUpdateDTO;
import uz.pdp.online_education.repository.QuestionRepository;
import uz.pdp.online_education.repository.QuizRepository;
import uz.pdp.online_education.service.interfaces.QuestionService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;

    @Override
    public QuestionResponseDTO create(QuestionCreateDTO createDTO) {
        Quiz quiz = quizRepository.findById(createDTO.getQuizId()).orElseThrow(
                () -> new EntityNotFoundException("Quiz not found"));

        Question question = new Question();
        question.setText(createDTO.getText());
        question.setType(createDTO.getType());


        quiz.addQuestion(question);

        Question save = questionRepository.save(question);
        return questionMapper.toDto(save);


    }

    @Override
    public QuestionResponseDTO getById(Long id) {
        return questionRepository.findById(id)
                .map(questionMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Question not found with id: " + id));
    }

    @Override
    public List<QuestionResponseDTO> getAllByQuizId(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(
                () -> new EntityNotFoundException("Quiz not found with id: " + quizId));
        return quiz.getQuestions()
                .stream()
                .map(questionMapper::toDto)
                .toList();
    }

    @Override
    public QuestionResponseDTO update(Long id, QuestionUpdateDTO updateDTO) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Question not found with id: " + id));

        question.setText(updateDTO.getText());
        question.setType(updateDTO.getType());

        return questionMapper.toDto(questionRepository.save(question));
    }

    @Override
    public void delete(Long id) {
        if (!questionRepository.existsById(id)){
            throw new EntityNotFoundException("Question not found with id: " + id);
        }
        questionRepository.deleteById(id);
    }
}
