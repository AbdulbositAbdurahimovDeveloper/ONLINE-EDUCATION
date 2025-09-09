package uz.pdp.online_education.service.interfaces;

import uz.pdp.online_education.model.Comment;
import uz.pdp.online_education.payload.comment.CommentCreateDto;
import uz.pdp.online_education.payload.comment.CommentResponseDto;
import uz.pdp.online_education.payload.comment.CommentUpdateDto;

import java.util.List;

public interface CommentService {


    CommentResponseDto update(Long commentId, CommentUpdateDto updateDto, Long currentUserId);

    void delete(Long commentId, Long currentUserId);

    List<CommentResponseDto> getAll();

    List<CommentResponseDto> getAllByCourseId(Long courseId);

    List<CommentResponseDto> getAllByLessonId(Long lessonId);

    Comment createComment(CommentCreateDto dto);
}