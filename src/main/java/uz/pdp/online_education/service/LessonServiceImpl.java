package uz.pdp.online_education.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.online_education.mapper.LessonMapper;
import uz.pdp.online_education.model.Module;
import uz.pdp.online_education.model.lesson.Lesson;
import uz.pdp.online_education.payload.lesson.LessonCreatDTO;
import uz.pdp.online_education.payload.lesson.LessonResponseDTO;
import uz.pdp.online_education.payload.lesson.LessonUpdateDTO;
import uz.pdp.online_education.repository.LessonRepository;
import uz.pdp.online_education.repository.ModuleRepository;
import uz.pdp.online_education.service.interfaces.LessonService;

@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;
    private final LessonMapper lessonMapper;
    private final ModuleRepository moduleRepository;

    @Override
    public Page<LessonResponseDTO> read(Integer page, Integer size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return lessonRepository.findAll(pageRequest).map(lessonMapper::toDTO);
    }

    @Override
    public LessonResponseDTO read(Long id) {
        return lessonRepository.findById(id).
                map(lessonMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

    }

    @Transactional
    @Override
    public LessonResponseDTO create(LessonCreatDTO lessonCreatDTO) {
        Module module = moduleRepository.findById(lessonCreatDTO.getModuleId()).orElseThrow(() -> new RuntimeException("Module not found"));


        Lesson lesson = new Lesson();
        lesson.setTitle(lessonCreatDTO.getTitle());
        lesson.setContent(lessonCreatDTO.getContent());
        lesson.setModule(module);
        lesson.setOrderIndex(lessonCreatDTO.getOrderIndex());
        lesson.setFree(lessonCreatDTO.isFree());

        Lesson saveLesson = lessonRepository.save(lesson);

        return lessonMapper.toDTO(saveLesson);

    }

    @Transactional
    @Override
    public LessonResponseDTO update(Long id, LessonUpdateDTO lessonUpdateDTO) {
        Lesson lesson = lessonRepository.findById(id).orElseThrow(() -> new RuntimeException("Lesson not found"));

        lesson.setTitle(lessonUpdateDTO.getTitle());
        lesson.setContent(lessonUpdateDTO.getContent());
        lesson.setOrderIndex(lessonUpdateDTO.getOrderIndex());
        lesson.setFree(lessonUpdateDTO.isFree());
        Lesson saveLesson = lessonRepository.save(lesson);
        return lessonMapper.toDTO(saveLesson);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        Lesson lesson = lessonRepository.findById(id).orElseThrow(() -> new RuntimeException("Lesson not found"));
        lessonRepository.delete(lesson);
    }
}
