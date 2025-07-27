package uz.pdp.online_education.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service; // <-- MANA SHU ANNOTATSIYANI QO'SHING
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.online_education.enums.TransactionStatus;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.model.Module;
import uz.pdp.online_education.model.lesson.Lesson;
import uz.pdp.online_education.repository.CourseRepository;
import uz.pdp.online_education.repository.LessonRepository;
import uz.pdp.online_education.repository.PaymentRepository;
import uz.pdp.online_education.service.interfaces.PurchaseService;

import java.util.List;

@Service // <-- BU ENG MUHIM QATOR!
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {

    private final PaymentRepository paymentRepository;
    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean hasLessonAccess(Long userId, Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Dars topilmadi: " + lessonId));

        if (lesson.getModule() == null) {
            return true;
        }
        Long moduleId = lesson.getModule().getId();

        return paymentRepository.existsByUserIdAndModuleIdAndStatus(userId, moduleId, TransactionStatus.SUCCESS);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasSufficientCourseAccess(Long userId, Long courseId, double requiredPercentage) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Kurs topilmadi: " + courseId));

        List<Module> modules = course.getModules();
        if (modules == null || modules.isEmpty()) {
            return true;
        }
        int totalModulesInCourse = modules.size();

        long purchasedModulesCount = paymentRepository.countPurchasedModulesInCourse(userId, courseId);

        double purchasedPercentage = (double) purchasedModulesCount / totalModulesInCourse;

        return purchasedPercentage >= requiredPercentage;
    }
}