package uz.pdp.online_education.service.interfaces;

import org.springframework.transaction.annotation.Transactional;
import uz.pdp.online_education.model.User;

public interface QuizService {

    @Transactional(readOnly = true)
    boolean isQuizAccessibleToUser(Long quizId, User user);
}
