package uz.pdp.online_education.service.interfaces;

import uz.pdp.online_education.payload.comment.CommentCreateDto;
import uz.pdp.online_education.payload.comment.CommentResponseDto;
import uz.pdp.online_education.payload.comment.CommentUpdateDto;

import java.util.List;

public interface CommentService {

    // Yangi komment yaratish
    CommentResponseDto create(CommentCreateDto createDto, Long currentUserId);

    // Kommentni tahrirlash
    CommentResponseDto update(Long commentId, CommentUpdateDto updateDto, Long currentUserId);

    // Kommentni o'chirish
    void delete(Long commentId, Long currentUserId);

    // Barcha asosiy kommentlarni olish
    List<CommentResponseDto> getAll();

    // Kurs bo'yicha barcha kommentlarni olish
    List<CommentResponseDto> getAllByCourseId(Long courseId);

    // Dars bo'yicha barcha kommentlarni olish
    List<CommentResponseDto> getAllByLessonId(Long lessonId);
}