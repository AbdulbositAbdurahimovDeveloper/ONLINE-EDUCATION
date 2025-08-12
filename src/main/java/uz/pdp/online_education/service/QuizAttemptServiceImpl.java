package uz.pdp.online_education.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.online_education.enums.AttemptStatus;
import uz.pdp.online_education.exceptions.DataConflictException;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.mapper.AnswerOptionMapper;
import uz.pdp.online_education.mapper.QuestionMapper;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.model.quiz.*;
import uz.pdp.online_education.payload.quiz.AnswerOptionResponseDTO;
import uz.pdp.online_education.payload.quiz.QuestionResponseDTO;
import uz.pdp.online_education.payload.quiz.test.*;
import uz.pdp.online_education.repository.*;
import uz.pdp.online_education.service.interfaces.QuizAttemptService;
import uz.pdp.online_education.service.interfaces.QuizService;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizAttemptServiceImpl implements QuizAttemptService {


    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;
    private final AnswerOptionRepository answerOptionRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final AnswerOptionMapper answerOptionMapper;
    private final QuizService quizService;


    @Override
    @Transactional
    public StartAttemptResponseDTO startAttempt(Long quizId, User user) {
        boolean isAccessible = quizService.isQuizAccessibleToUser(quizId, user);
        if (!isAccessible) {
            throw new SecurityException("User does not have access to start this quiz. Payment required.");
        }

        quizAttemptRepository.findByUserIdAndQuizIdAndStatus(user.getId(), quizId, AttemptStatus.IN_PROGRESS)
                .ifPresent(attempt -> {
                    throw new DataConflictException("You already have an unfinished attempt for this quiz. Please complete it first.");
                });

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new EntityNotFoundException("Quiz not found with id: " + quizId));

        QuizAttempt newQuizAttempt = new QuizAttempt();
        newQuizAttempt.setUser(user);
        newQuizAttempt.setQuiz(quiz);
        newQuizAttempt.setStartTime(LocalDateTime.now());
        newQuizAttempt.setStatus(AttemptStatus.IN_PROGRESS);
        newQuizAttempt.setTotalQuestions(quiz.getQuestions().size());

        QuizAttempt savedAttempt = quizAttemptRepository.save(newQuizAttempt);

        Question firstQuestion = questionRepository.findFirstByQuizIdOrderByIdAsc(quizId).orElse(null);
        QuestionResponseDTO firstQuestionDto = (firstQuestion != null) ? questionMapper.toDto(firstQuestion) : null;

        return new StartAttemptResponseDTO(savedAttempt.getId(), firstQuestionDto);
    }

    @Override
    @Transactional // <--- BU ANNOTATSIYA MAJBURIY!
    public AnswerResultDTO submitAnswer(Long attemptId, AnswerSubmissionDTO submissionDTO, User user) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new EntityNotFoundException("QuizAttempt not found with id: " + attemptId));

        if (!attempt.getUser().getId().equals(user.getId())) {
            throw new SecurityException("User does not have permission for this attempt.");
        }
        if (attempt.getStatus() == AttemptStatus.COMPLETED) {
            throw new DataConflictException("This quiz attempt has already been completed.");
        }

        Question question = questionRepository.findById(submissionDTO.getQuestionId())
                .orElseThrow(() -> new EntityNotFoundException("Question not found with id: " + submissionDTO.getQuestionId()));

        validateSelectedOptions(question, submissionDTO.getSelectedOptionIds());

        boolean isCorrect = checkAnswer(question, submissionDTO.getSelectedOptionIds());

        saveUserAnswer(attempt, question, submissionDTO.getSelectedOptionIds(), isCorrect);

        if (isCorrect) {
            attempt.setScore(attempt.getScore() + 1);
        }
        // @Transactional tufayli bu o'zgarish tranzaksiya oxirida avtomatik saqlanadi.

        Question nextQuestion = questionRepository.findFirstByQuizIdAndIdGreaterThanOrderByIdAsc(attempt.getQuiz().getId(), question.getId())
                .orElse(null);

        QuestionResponseDTO nextQuestionDTO = (nextQuestion != null) ? questionMapper.toDto(nextQuestion) : null;

        return new AnswerResultDTO(isCorrect, nextQuestionDTO);
    }



    @Override
    @Transactional
    public QuizResultDTO finishAttempt(Long attemptId, User user) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new EntityNotFoundException("Quiz attempt not found with id: " + attemptId));

        if (!attempt.getUser().getId().equals(user.getId())) {
            throw new DataConflictException("User does not have permission to finish this attempt.");
        }
        if (attempt.getStatus() == AttemptStatus.COMPLETED) {
            return getAttemptResult(attemptId, user); // Agar allaqachon tugagan bo'lsa, shunchaki natijani qaytaramiz
        }

        // BU YERDA ENDI HISOBLASH SHART EMAS, CHUNKI 'submitAnswer' BUNI QILIB BORDI.
        // AGAR 'score'ni OXIRIDA HISOBLAMOQCHI BO'LSAK, FAQAT SHU YERDA HISOBLAYMIZ.
        // HOZIRGI LOGIKAMIZ BO'YICHA, 'score' ALLAQACHON TO'G'RI.

        int score = attempt.getScore();
        int totalQuestions = attempt.getTotalQuestions();
        double percentage = (totalQuestions > 0) ? ((double) score * 100.0 / totalQuestions) : 0.0;

        attempt.setStatus(AttemptStatus.COMPLETED);
        attempt.setEndTime(LocalDateTime.now());
        attempt.setPercentage(percentage);

        // @Transactional tufayli 'attempt' obyekti avtomatik yangilanadi.

        return getAttemptResult(attemptId, user);
    }


    @Override
    @Transactional(readOnly = true)
    public QuizResultDTO getAttemptResult(Long attemptId, User user) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new EntityNotFoundException("Quiz attempt not found with id: " + attemptId));

        if (!attempt.getUser().getId().equals(user.getId())) {
            throw new SecurityException("User does not have permission to view this result.");
        }

        List<UserAnswer> userAnswers = userAnswerRepository.findAllWithDetailsByAttemptId(attemptId);
        List<UserAnswerResultDTO> answerResults = userAnswers.stream()
                .map(this::toUserAnswerResultDto)
                .collect(Collectors.toList());

        return new QuizResultDTO(
                attempt.getId(),
                attempt.getQuiz().getTitle(),
                attempt.getStartTime(),
                attempt.getEndTime(),
                attempt.getTotalQuestions(),
                attempt.getScore(),
                attempt.getPercentage(),
                answerResults
        );
    }

    private void validateSelectedOptions(Question question, Set<Long> selectedOptionIds) {
        if (selectedOptionIds == null || selectedOptionIds.isEmpty()) {
            // Agar foydalanuvchi hech qanday javob tanlamagan bo'lsa,
            // bu ham xato bo'lishi mumkin. Hozircha o'tkazib yuboramiz.
            return;
        }

        // Savolning o'ziga tegishli bo'lgan barcha javob variantlarining ID'larini yig'ib olamiz
        Set<Long> actualOptionIds = question.getOptions().stream()
                .map(AnswerOption::getId)
                .collect(Collectors.toSet());

        // Foydalanuvchi yuborgan ID'lar to'plami, haqiqiy ID'lar to'plamining
        // ichida to'liq yotishini tekshiramiz.
        if (!actualOptionIds.containsAll(selectedOptionIds)) {
            throw new DataConflictException("One or more selected options do not belong to the specified question.");
        }
    }

    private static double getPercentage(User user, QuizAttempt attempt) {
        if (!attempt.getUser().getId().equals(user.getId())) {
            throw new DataConflictException("User does not have permission to finish this attempt.");
        }
        if (attempt.getStatus() == AttemptStatus.COMPLETED) {
            throw new DataConflictException("This quiz attempt has already been completed.");
        }

        // 2. Natijalarni hisoblaymiz (score allaqachon 'submitAnswer'da hisoblangan)
        // Agar 'score'ni oxirida hisoblamoqchi bo'lsak, bu yerda logikani yozamiz
        int score = attempt.getScore();
        int totalQuestions = attempt.getTotalQuestions();
        return (totalQuestions > 0) ? ((double) score / totalQuestions) * 100.0 : 0.0;
    }

    // Yordamchi mapper metod
    private UserAnswerResultDTO toUserAnswerResultDto(UserAnswer userAnswer) {
        Question question = userAnswer.getQuestion();

        Set<AnswerOptionResponseDTO> selectedOptionsDto = userAnswer.getSelectedOptions().stream()
                .map(answerOptionMapper::toDto)
                .collect(Collectors.toSet());

        Set<AnswerOptionResponseDTO> correctOptionsDto = question.getOptions().stream()
                .filter(AnswerOption::isCorrect)
                .map(answerOptionMapper::toDto)
                .collect(Collectors.toSet());

        return new UserAnswerResultDTO(
                question.getId(),
                question.getText(),
                selectedOptionsDto,
                correctOptionsDto,
                userAnswer.isCorrect()
        );
    }

    private void saveUserAnswer(QuizAttempt quizAttempt, Question question, Set<Long> selectedOptionIds, boolean isCorrect) {
        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setAttempt(quizAttempt);
        userAnswer.setQuestion(question);

        Set<AnswerOption> selectedOptions = new HashSet<>(answerOptionRepository.findAllById(selectedOptionIds));

        userAnswer.setSelectedOptions(selectedOptions);
        userAnswer.setCorrect(isCorrect);

        userAnswerRepository.save(userAnswer);

    }

    private boolean checkAnswer(Question question, Set<Long> selectedOptionIds) {

        Set<Long> correctOptIds = question.getOptions().stream()
                .filter(AnswerOption::isCorrect)
                .map(AnswerOption::getId)
                .collect(Collectors.toSet());

        return correctOptIds.equals(selectedOptionIds);

    }
}
