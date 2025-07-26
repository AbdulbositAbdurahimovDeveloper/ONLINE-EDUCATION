package uz.pdp.online_education.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.mapper.LessonMapper;
import uz.pdp.online_education.model.Module;
import uz.pdp.online_education.model.lesson.Lesson;
import uz.pdp.online_education.payload.lesson.LessonCreatDTO;
import uz.pdp.online_education.payload.lesson.LessonOrderUpdateDTO;
import uz.pdp.online_education.payload.lesson.LessonResponseDTO;
import uz.pdp.online_education.payload.lesson.LessonUpdateDTO;
import uz.pdp.online_education.repository.LessonRepository;
import uz.pdp.online_education.repository.ModuleRepository;
import uz.pdp.online_education.service.interfaces.LessonService;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;
    private final LessonMapper lessonMapper;
    private final ModuleRepository moduleRepository;

    @Override
    public Page<LessonResponseDTO> read(Integer page, Integer size) {
        Sort sort = Sort.by(Sort.Direction.ASC, Lesson.Fields.orderIndex);
        PageRequest pageRequest = PageRequest.of(page, size,sort);
        return lessonRepository.findAll(pageRequest).map(lessonMapper::toDTO);
    }

    @Override
    public LessonResponseDTO read(Long id) {
        return lessonRepository.findById(id).
                map(lessonMapper::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Lesson not found with id: " + id));

    }

    @Transactional
    @Override
    public LessonResponseDTO create(LessonCreatDTO lessonCreatDTO) {
        Module module = moduleRepository.findById(lessonCreatDTO.getModuleId())
                .orElseThrow(() -> new EntityNotFoundException("Module not found with id: " + lessonCreatDTO.getModuleId()));


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
        Lesson lesson = lessonRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Lesson not found with id: " + id));

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
        if (!lessonRepository.existsById(id)) {
            throw new EntityNotFoundException("Lesson not found with id: " + id);
        }
        lessonRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void updateOrder(Long moduleId, List<Long> orderedLessonIds) {
        // 1. Modulga tegishli barcha darslarni bazadan bitta so'rov bilan olamiz.
        List<Lesson> lessonsInDb = lessonRepository.findAllByModuleId(moduleId);

        // 2. O'lchamlar mosligini tekshiramiz. Bu frontend xatosining oldini oladi.
        if (orderedLessonIds.size() != lessonsInDb.size()) {
            throw new IllegalStateException("The number of sent IDs does not match the number of lessons in the module.");
        }

        // 3. Darslarni tezkor qidirish uchun Map'ga o'tkazamiz (ID -> Lesson).
        Map<Long, Lesson> lessonMap = lessonsInDb.stream()
                .collect(Collectors.toMap(Lesson::getId, Function.identity()));

        // 4. Ma'lumotlar yaxlitligini tekshiramiz: Kelgan ID'lar to'plami
        //    bazadagi ID'lar to'plamiga to'liq mos kelishini tekshiramiz.
        //    Bu takrorlangan yoki begona ID'lar yuborilishidan himoya qiladi.
        if (!lessonMap.keySet().equals(new HashSet<>(orderedLessonIds))) {
            throw new IllegalStateException("The provided lesson IDs are invalid or do not match the module's lessons.");
        }

        // 5. Yangi tartib raqamlarini serverda, xavfsiz tarzda generatsiya qilamiz.
        for (int i = 0; i < orderedLessonIds.size(); i++) {
            Long lessonId = orderedLessonIds.get(i);
            Lesson lessonToUpdate = lessonMap.get(lessonId);
            lessonToUpdate.setOrderIndex(i); // Yangi tartib raqami = ro'yxatdagi o'rni
        }

        // @Transactional tufayli o'zgartirilgan barcha lesson'lar tranzaksiya
        // yakunida avtomatik ravishda bazaga saqlanadi.
    }
}
