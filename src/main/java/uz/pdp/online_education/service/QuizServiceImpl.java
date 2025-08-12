package uz.pdp.online_education.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.model.Module;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.model.quiz.Quiz;
import uz.pdp.online_education.repository.ModuleRepository;
import uz.pdp.online_education.repository.PaymentRepository;
import uz.pdp.online_education.repository.QuizRepository;
import uz.pdp.online_education.service.interfaces.QuizService;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final PaymentRepository paymentRepository;
    private final QuizRepository quizRepository;
    private final ModuleRepository moduleRepository;

    @Transactional(readOnly = true)
    @Override
    public boolean isQuizAccessibleToUser(Long quizId, User user) {
        // 1. Quiz'ni va unga bog'liq modulni samarali topamiz
        Quiz quiz = quizRepository.findByIdWithModule(quizId)
                .orElseThrow(() -> new EntityNotFoundException("Quiz not found with id: " + quizId));

        Long id = quiz.getQuizContent().getLesson().getModule().getId();

        Module module = moduleRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Module not found with id: " + id));

        // 2. Agar modul bepul bo'lsa, hamma uchun ochiq
        if (module.getPrice() == null || module.getPrice() <= 0) {
            return true;
        }

        // 3. Agar pullik bo'lsa, foydalanuvchi to'lov qilganmi-yo'qligini tekshiramiz
        return paymentRepository.existsByUser_IdAndModule_Id(user.getId(), module.getId());
    }
}
