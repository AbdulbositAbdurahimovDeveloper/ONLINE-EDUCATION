package uz.pdp.online_education.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.exceptions.ForbiddenException;
import uz.pdp.online_education.exceptions.InvalidCommentTargetException;
import uz.pdp.online_education.exceptions.UserAccessDeniedException;
import uz.pdp.online_education.mapper.CommentMapper; // MAPPER'NI IMPORT QILAMIZ
import uz.pdp.online_education.model.Comment;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.model.Module;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.model.lesson.Lesson;
import uz.pdp.online_education.payload.comment.CommentCreateDto;
import uz.pdp.online_education.payload.comment.CommentResponseDto;
import uz.pdp.online_education.payload.comment.CommentUpdateDto;
import uz.pdp.online_education.repository.CommentRepository;
import uz.pdp.online_education.repository.CourseRepository;
import uz.pdp.online_education.repository.LessonRepository;
import uz.pdp.online_education.repository.UserRepository;
import uz.pdp.online_education.service.interfaces.CommentService;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final CommentMapper commentMapper;
    private final UserRepository userRepository;


    @Override
    @Transactional
    public CommentResponseDto update(Long commentId, CommentUpdateDto updateDto, Long currentUserId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Komment topilmadi: " + commentId));

        if (!comment.getUser().getId().equals(currentUserId)) {
            throw new ForbiddenException("Siz faqat o'zingizning kommentlaringizni tahrirlay olasiz.");
        }

        // Mavjud entity'ni DTO bilan yangilash
        commentMapper.updateEntityFromDto(updateDto, comment);

        Comment updatedComment = commentRepository.save(comment);
        return commentMapper.toDto(updatedComment);
    }

    @Override
    @Transactional
    public void delete(Long commentId, Long currentUserId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Komment topilmadi: " + commentId));

        if (!comment.getUser().getId().equals(currentUserId)) {
            throw new ForbiddenException("Siz faqat o'zingizning kommentlaringizni o'chira olasiz.");
        }

        commentRepository.delete(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getAll() {
        List<Comment> comments = commentRepository.findAllByParentIsNull();
        return commentMapper.toDtoList(comments); // Mapper orqali listni konvertatsiya qilish
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getAllByCourseId(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new EntityNotFoundException("Kurs topilmadi: " + courseId);
        }
        List<Comment> comments = commentRepository.findAllByCourseIdAndParentIsNull(courseId);
        return commentMapper.toDtoList(comments);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getAllByLessonId(Long lessonId) {
        if (!lessonRepository.existsById(lessonId)) {
            throw new EntityNotFoundException("Dars topilmadi: " + lessonId);
        }
        List<Comment> comments = commentRepository.findAllByLessonIdAndParentIsNull(lessonId);
        return commentMapper.toDtoList(comments);
    }











    @Override
    @Transactional
    public Comment createComment(CommentCreateDto dto) {
        // 1. Foydalanuvchini topish
        User currentUser = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Foydalanuvchi topilmadi: " + dto.getUserId()));

        Comment newComment = new Comment();
        newComment.setText(dto.getText());
        newComment.setUser(currentUser);

        Course targetCourse = null;
        Lesson targetLesson = null;

        // 2. Kommentning maqsadini aniqlash (Course, Lesson yoki Parent)
        if (dto.getParentId() != null) {
            // Agar parentId mavjud bo'lsa, bu javob (reply) hisoblanadi
            Comment parentComment = commentRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("Javob berilayotgan komment topilmadi: " + dto.getParentId()));

            // Javob kommentining maqsadini asosiy kommentdan meros qilib olish
            targetCourse = parentComment.getCourse();
            targetLesson = parentComment.getLesson();

            // Javob kommentini asosiy kommentga bog'lash
            newComment.setParent(parentComment);
            newComment.setCourse(targetCourse); // Parentning kursini bog'laymiz
            newComment.setLesson(targetLesson); // Parentning darsini bog'laymiz

        } else {
            // Yangi komment bo'lsa, courseId yoki lessonId orqali maqsadni aniqlash
            if (dto.getCourseId() != null && dto.getLessonId() != null) {
                throw new InvalidCommentTargetException("Komment bir vaqtning o'zida ham kursga, ham darsga tegishli bo'la olmaydi. Faqat bittasini tanlang.");
            }
            if (dto.getCourseId() == null && dto.getLessonId() == null) {
                throw new IllegalArgumentException("Komment uchun maqsad (courseId yoki lessonId) majburiy.");
            }

            if (dto.getCourseId() != null) {
                targetCourse = courseRepository.findById(dto.getCourseId())
                        .orElseThrow(() -> new IllegalArgumentException("Kurs topilmadi: " + dto.getCourseId()));
                newComment.setCourse(targetCourse);
            } else { // dto.getLessonId() != null
                targetLesson = lessonRepository.findById(dto.getLessonId())
                        .orElseThrow(() -> new IllegalArgumentException("Dars topilmadi: " + dto.getLessonId()));
                newComment.setLesson(targetLesson);
            }
        }

        // 3. Foydalanuvchining kirish huquqini tekshirish
        if (targetCourse != null) {
            // Kursga komment qoldirish uchun kamida 20% modul sotib olingan bo'lishi kerak
            if (!hasUserPurchasedAtLeast20PercentOfCourse(currentUser, targetCourse)) {
                throw new UserAccessDeniedException("Kursga komment qoldirish uchun kursning kamida 20% modulini sotib olgan bo'lishingiz kerak.");
            }
        } else if (targetLesson != null) {
            // Darsga komment qoldirish uchun shu dars joylashgan modul sotib olingan bo'lishi kerak
            if (!hasUserPurchasedModuleForLesson(currentUser, targetLesson)) {
                throw new UserAccessDeniedException("Ushbu darsga komment qoldirish uchun uning modulini sotib olgan bo'lishingiz kerak.");
            }
        } else {
            throw new IllegalStateException("Komment maqsadini aniqlashda kutilmagan xato yuz berdi.");
        }

        // 4. Kommentni saqlash
        return commentRepository.save(newComment);
    }

    /**
     * Foydalanuvchi berilgan kursning kamida 20% modulini sotib olganligini tekshiradi.
     * Bu metod Course entitysining 'modules' listidan foydalanadi.
     *
     * @param user Foydalanuvchi obyekti
     * @param course Kurs obyekti
     * @return Agar 20% yoki undan ko'p modul sotib olingan bo'lsa true, aks holda false
     */
    private boolean hasUserPurchasedAtLeast20PercentOfCourse(User user, Course course) {
        // Kursdagi jami modullar sonini olish
        if (course.getModules() == null || course.getModules().isEmpty()) {
            // Agar kursda modullar bo'lmasa, komment qoldirishga ruxsat berish
            // Yoki bu yerda false qaytarish mumkin, agar modullarsiz kursga komment qoldirish mumkin bo'lmasa.
            // Hozirgi holatda ruxsat beriladi.
            return true;
        }

        // Kursdagi barcha modullarning ID'larini to'plash
        Set<Long> courseModuleIds = course.getModules().stream()
                .map(Module::getId)
                .collect(Collectors.toSet());

        long totalModulesInCourse = courseModuleIds.size();

        if (totalModulesInCourse == 0) {
            return true; // Modullar yo'q bo'lsa, avtomatik ruxsat berish
        }

        // Foydalanuvchi sotib olgan modullar ID'larini olish
        // Bu yerda getUserPurchasedModuleIds() yordamchi metodidan foydalaniladi.
        Set<Long> userPurchasedModuleIds = getUserPurchasedModuleIds(user);
        if (userPurchasedModuleIds.isEmpty()) { // isEmpty() tekshiruvi null tekshiruvidan yaxshiroq
            return false; // Foydalanuvchi hech qanday modul sotib olmagan
        }

        // Foydalanuvchi sotib olgan va joriy kursga tegishli bo'lgan modullar sonini hisoblash
        long purchasedModulesInThisCourse = userPurchasedModuleIds.stream()
                .filter(courseModuleIds::contains) // Faqat kursga tegishli sotib olingan modullarni hisoblaymiz
                .count();

        double purchasedPercentage = (double) purchasedModulesInThisCourse / totalModulesInCourse * 100;
        System.out.println("Foydalanuvchi " + user.getUsername() + " kursning " + purchasedPercentage + "% modulini sotib olgan.");
        return purchasedPercentage >= 20.0;
    }

    /**
     * Foydalanuvchi berilgan dars joylashgan modulni sotib olganligini tekshiradi.
     * Bu metod Lesson entitysining 'module' maydonidan foydalanadi.
     *
     * @param user Foydalanuvchi obyekti
     * @param lesson Dars obyekti
     * @return Agar modul sotib olingan bo'lsa true, aks holda false
     */
    private boolean hasUserPurchasedModuleForLesson(User user, Lesson lesson) {
        // Darsning qaysi modulga tegishli ekanligini aniqlash
        if (lesson.getModule() == null || lesson.getModule().getId() == null) {
            return false; // Modul IDsi aniqlanmagan darsga komment qoldirishga ruxsat bermaslik
        }
        Long lessonModuleId = lesson.getModule().getId();

        // Foydalanuvchi sotib olgan modullar ro'yxatida shu modul borligini tekshirish
        // Bu yerda getUserPurchasedModuleIds() yordamchi metodidan foydalaniladi.
        Set<Long> userPurchasedModuleIds = getUserPurchasedModuleIds(user);
        return !userPurchasedModuleIds.isEmpty() && userPurchasedModuleIds.contains(lessonModuleId);
    }

    /**
     * Foydalanuvchining sotib olgan modullari ID'larini bazadan olib keladi.
     * Bu metod hozircha stub hisoblanadi va haqiqiy loyihada ModuleEnrollment yoki Payment entitylaridan
     * ma'lumot olib kelish logikasi bilan to'ldirilishi kerak.
     *
     * @param user Foydalanuvchi obyekti
     * @return Foydalanuvchi sotib olgan modullar ID'lari to'plami
     */
    private Set<Long> getUserPurchasedModuleIds(User user) {
        // TODO: Haqiqiy loyihada bu yerga ModuleEnrollmentRepository yoki PaymentRepository orqali
        // foydalanuvchining sotib olgan modullari ID'larini olish logikasini yozing.
        // Masalan:
        // return moduleEnrollmentRepository.findByUserId(user.getId()).stream()
        //     .map(ModuleEnrollment::getModuleId) // Agar ModuleEnrollmentda moduleId bo'lsa
        //     .collect(Collectors.toSet());
        // Yoki Payment entitysidan foydalanib.

        // Hozircha bo'sh to'plam qaytaramiz, chunki User entitysida bu ma'lumot yo'q.
        // Bu metodni to'ldirmaguningizcha, foydalanuvchilar hech qanday modul sotib olmagan deb hisoblanadi
        // va komment qoldira olmaydilar.
        System.out.println("DEBUG: getUserPurchasedModuleIds for user " + user.getId() + " called. Implement actual fetching logic here.");
        return Collections.emptySet(); // Bo'sh o'qilmaydigan to'plam qaytarish
    }

}