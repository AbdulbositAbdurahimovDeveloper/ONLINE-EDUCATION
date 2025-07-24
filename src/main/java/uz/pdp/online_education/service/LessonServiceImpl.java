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
    public void updateOrder(Long moduleId, List<LessonOrderUpdateDTO> newOrderList) {

        // 1. DTO'dan kelgan barcha lessonId'larni yig'ib olamiz
        Set<Long> lessonIdsFromDto = newOrderList.stream()
                .map(LessonOrderUpdateDTO::getId)
                .collect(Collectors.toSet());

        // 2. O'sha ID'dagi barcha darslarni bazadan bitta so'rov bilan olamiz
        // Bu N+1 muammosining oldini oladi
        List<Lesson> lessonsToUpdate = lessonRepository.findAllModuleIdAndIdIn(moduleId, lessonIdsFromDto);

        // 3. Xavfsizlik tekshiruvi: DTO'da kelgan barcha darslar bazada topildimi
        // va ular haqiqatan ham shu modulga tegishlimi?
        if (lessonsToUpdate.size() != lessonIdsFromDto.size()) {
            throw new IllegalStateException("Some lessons do not exist or do not belong to the specified module.");
        }

        // 4. Qidiruvni tezlashtirish uchun darslarni Map'ga o'girib olamiz (ID -> Lesson)
        Map<Long, Lesson> lessonMap = lessonsToUpdate.stream()
                .collect(Collectors.toMap(Lesson::getId, Function.identity()));

        // 5. Har bir darsning orderIndex'ini yangi qiymat bilan yangilab chiqamiz
        newOrderList.forEach(dto -> {
            Lesson lesson = lessonMap.get(dto.getId());
            if (lesson != null) {
                lesson.setOrderIndex(dto.getOrderIndex());
            }
        });

        // Tranzaksiya tugaganda, Hibernate barcha o'zgartirilgan darslarni avtomatik ravishda
        // bitta `batch update` so'rovida bazaga yozadi. .save() chaqirish shart emas.
    }
}
