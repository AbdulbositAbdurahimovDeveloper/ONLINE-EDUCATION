package uz.pdp.online_education.mapper;

import uz.pdp.online_education.model.lesson.Lesson;
import uz.pdp.online_education.payload.lesson.LessonResponseDTO;

public interface LessonMapper {
    LessonResponseDTO toDTO(Lesson lesson);

}