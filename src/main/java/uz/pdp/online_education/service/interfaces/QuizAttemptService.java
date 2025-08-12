package uz.pdp.online_education.service.interfaces;

import uz.pdp.online_education.model.User;
import uz.pdp.online_education.payload.quiz.test.AnswerResultDTO;
import uz.pdp.online_education.payload.quiz.test.AnswerSubmissionDTO;
import uz.pdp.online_education.payload.quiz.test.QuizResultDTO;
import uz.pdp.online_education.payload.quiz.test.StartAttemptResponseDTO;


public interface QuizAttemptService {

    StartAttemptResponseDTO startAttempt(Long quizId, User user);

    AnswerResultDTO submitAnswer(Long attemptId, AnswerSubmissionDTO submissionDTO, User user);

    QuizResultDTO finishAttempt(Long attemptId, User user);

    QuizResultDTO getAttemptResult(Long attemptId, User user);

}
