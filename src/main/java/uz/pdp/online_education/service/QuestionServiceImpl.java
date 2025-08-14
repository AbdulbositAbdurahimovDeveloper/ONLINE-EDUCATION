package uz.pdp.online_education.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.online_education.enums.QuestionType;
import uz.pdp.online_education.exceptions.DataConflictException;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.mapper.QuestionMapper;
import uz.pdp.online_education.model.lesson.Lesson;
import uz.pdp.online_education.model.quiz.AnswerOption;
import uz.pdp.online_education.model.quiz.Question;
import uz.pdp.online_education.model.quiz.Quiz;
import uz.pdp.online_education.payload.quiz.*;
import uz.pdp.online_education.repository.PaymentRepository;
import uz.pdp.online_education.repository.QuestionRepository;
import uz.pdp.online_education.repository.QuizRepository;
import uz.pdp.online_education.service.interfaces.QuestionService;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public QuestionResponseDTO createWithAnswers(QuestionCreateWithAnswersDTO createDTO) {
        validateAnswerOptions(createDTO.getType(), createDTO.getOptions());

        Quiz quiz = quizRepository.findById(createDTO.getQuizId())
                .orElseThrow(() -> new EntityNotFoundException("Quiz not found with id: " + createDTO.getQuizId()));

        Question question = new Question();
        question.setText(createDTO.getText());
        question.setType(createDTO.getType());

        createDTO.getOptions().forEach(optionDto -> {
            AnswerOption answerOption = new AnswerOption();
            answerOption.setText(optionDto.getText());
            answerOption.setCorrect(optionDto.getIsCorrect());
            question.addOption(answerOption);
        });

        quiz.addQuestion(question);
        Question savedQuestion = questionRepository.save(question);
        return questionMapper.toDto(savedQuestion);
    }

    private void validateAnswerOptions(QuestionType type, List<? extends AnswerOptionData> options) {
        long correctOptionsCount = options.stream()
                .filter(AnswerOptionData::getIsCorrect) // isCorrect() o'rniga getIsCorrect()
                .count();

        if (type == QuestionType.SINGLE_CHOICE && correctOptionsCount != 1) {
            throw new DataConflictException("A single-choice question must have exactly one correct answer.");
        }

        if (type == QuestionType.MULTIPLE_CHOICE && correctOptionsCount < 1) {
            throw new DataConflictException("A multiple-choice question must have at least one correct answer.");
        }
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
    @Transactional
    public QuestionResponseDTO updateWithAnswers(Long questionId, QuestionUpdateWithAnswersDTO updateDTO) {
        // 0. Biznes qoidalarini avvaldan tekshiramiz
        validateAnswerOptions(updateDTO.getType(), updateDTO.getOptions());

        // 1. Asosiy savolni bazadan uning o'zining ID'si orqali topamiz
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("Question not found with id: " + questionId));

        // 2. Savolning o'zini yangilaymiz
        question.setText(updateDTO.getText());
        question.setType(updateDTO.getType());

        // 3. Javoblarni sinxronizatsiya qilamiz (bu logika to'g'ri edi)
        Map<Long, AnswerOption> existingOptionsMap = question.getOptions().stream()
                .collect(Collectors.toMap(AnswerOption::getId, Function.identity()));

        Set<Long> dtoOptionIds = updateDTO.getOptions().stream()
                .map(AnswerOptionUpdateNestedDTO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // O'CHIRISH: Bazada bor, DTO'da yo'q javoblarni o'chiramiz
        question.getOptions().removeIf(option -> !dtoOptionIds.contains(option.getId()));

        // YANGILASH va QO'SHISH
        for (AnswerOptionUpdateNestedDTO optionDto : updateDTO.getOptions()) {
            if (optionDto.getId() != null) {
                // Yangilash
                AnswerOption existingOption = existingOptionsMap.get(optionDto.getId());
                if (existingOption != null) {
                    existingOption.setText(optionDto.getText());
                    existingOption.setCorrect(optionDto.getIsCorrect());
                }
            } else {
                // Yangi qo'shish
                AnswerOption newOption = new AnswerOption();
                newOption.setText(optionDto.getText());
                newOption.setCorrect(optionDto.getIsCorrect());
                question.addOption(newOption);
            }
        }

        // O'zgarishlar @Transactional tufayli avtomatik saqlanadi
        return questionMapper.toDto(question);
    }


    @Override
    public void delete(Long id) {
        if (!questionRepository.existsById(id)) {
            throw new EntityNotFoundException("Question not found with id: " + id);
        }
        questionRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true) // To'lovlar ro'yxatini yuklash uchun tranzaksiya kerak
    public boolean isUserQuestionBought(String username, Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("Question not found with id: " + questionId));

        Lesson lesson = question.getQuiz().getQuizContent().getLesson();

        if (lesson.isFree()) {
            return true;
        }

        boolean hasPaid = paymentRepository.existsByUser_UsernameAndModule_Id(username, lesson.getModule().getId());

        if (hasPaid) {
            return true;
        }

        // Agar yuqoridagi shartlarning hech biri bajarilmasa, xatolik beramiz.
        throw new DataConflictException("Tolov qiling.");
    }
}
