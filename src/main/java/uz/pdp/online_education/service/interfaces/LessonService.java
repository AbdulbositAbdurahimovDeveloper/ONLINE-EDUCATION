package uz.pdp.online_education.service.interfaces;

import org.springframework.data.domain.Page;
import uz.pdp.online_education.payload.lesson.LessonCreatDTO;
import uz.pdp.online_education.payload.lesson.LessonOrderUpdateDTO;
import uz.pdp.online_education.payload.lesson.LessonResponseDTO;
import uz.pdp.online_education.payload.lesson.LessonUpdateDTO;

import java.util.List;

public interface LessonService {

    Page<LessonResponseDTO> read(Integer page, Integer size);

    LessonResponseDTO read(Long id);

    LessonResponseDTO create(LessonCreatDTO lessonCreatDTO);

    LessonResponseDTO update(Long id, LessonUpdateDTO lessonUpdateDTO);

    void delete(Long id);

    void updateOrder(Long moduleId, List<LessonOrderUpdateDTO> newOrderList);
}
