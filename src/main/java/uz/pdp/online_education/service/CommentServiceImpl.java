package uz.pdp.online_education.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.online_education.exceptions.DataValidationException;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.exceptions.ForbiddenException;
import uz.pdp.online_education.mapper.CommentMapper; // MAPPER'NI IMPORT QILAMIZ
import uz.pdp.online_education.model.Comment;
import uz.pdp.online_education.model.Course;
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
import uz.pdp.online_education.service.interfaces.PurchaseService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final PurchaseService purchaseService;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public CommentResponseDto create(CommentCreateDto createDto, Long currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("Foydalanuvchi topilmadi: " + currentUserId));

        if (createDto.getCommentableId() == null || createDto.getCommentableType() == null || createDto.getCommentableType().isBlank()) {
            throw new DataValidationException("Komment 'course' yoki 'lesson' ga bog'langan bo'lishi shart.");
        }

        // DTO'dan Entity'ga o'tkazish mapper orqali
        Comment comment = commentMapper.toEntity(createDto);

        // Qolgan murakkab maydonlarni servisda o'rnatamiz
        comment.setUser(user);

        String type = createDto.getCommentableType().toLowerCase();

        if ("course".equals(type)) {
            Course course = courseRepository.findById(createDto.getCommentableId())
                    .orElseThrow(() -> new EntityNotFoundException("Kurs topilmadi: " + createDto.getCommentableId()));

            if (!purchaseService.hasSufficientCourseAccess(currentUserId, course.getId(), 0.20)) {
                throw new ForbiddenException("Komment yozish uchun kurs modullarining kamida 20% qismini sotib olishingiz kerak.");
            }
            comment.setCourse(course);

        } else if ("lesson".equals(type)) {
            Lesson lesson = lessonRepository.findById(createDto.getCommentableId())
                    .orElseThrow(() -> new EntityNotFoundException("Dars topilmadi: " + createDto.getCommentableId()));

            if (!purchaseService.hasLessonAccess(currentUserId, lesson.getId())) {
                throw new ForbiddenException("Komment yozish uchun ushbu dars joylashgan modulni sotib olishingiz kerak.");
            }
            comment.setLesson(lesson);
        } else {
            throw new DataValidationException("'commentableType' faqat 'course' yoki 'lesson' bo'lishi mumkin.");
        }

        if (createDto.getParentId() != null) {
            Comment parentComment = commentRepository.findById(createDto.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("Ota-komment topilmadi: " + createDto.getParentId()));
            comment.setParent(parentComment);
        }

        Comment savedComment = commentRepository.save(comment);

        // Entity'dan DTO'ga o'tkazish mapper orqali
        return commentMapper.toDto(savedComment);
    }

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
}