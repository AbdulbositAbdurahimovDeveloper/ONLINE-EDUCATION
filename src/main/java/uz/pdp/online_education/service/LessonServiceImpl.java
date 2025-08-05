package uz.pdp.online_education.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.online_education.exceptions.DataConflictException;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.mapper.LessonMapper;
import uz.pdp.online_education.model.Module;
import uz.pdp.online_education.model.lesson.Lesson;
import uz.pdp.online_education.payload.lesson.LessonCreatDTO;
import uz.pdp.online_education.payload.lesson.LessonResponseDTO;
import uz.pdp.online_education.payload.lesson.LessonUpdateDTO;
import uz.pdp.online_education.repository.LessonRepository;
import uz.pdp.online_education.repository.ModuleRepository;
import uz.pdp.online_education.service.interfaces.LessonService;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;
    private final LessonMapper lessonMapper;
    private final ModuleRepository moduleRepository;

//    @Override
//    public PageDTO<LessonResponseDTO> read(Long moduleId,Integer page, Integer size) {
//        Sort sort = Sort.by(Sort.Direction.ASC, Lesson.Fields.orderIndex);
//        PageRequest pageRequest = PageRequest.of(page, size,sort);
//        Page<Lesson> lessons = lessonRepository.findAllByModule_Id(moduleId,pageRequest);
//
//        return new PageDTO<>(
//                lessons.getContent().stream().map(lessonMapper::toDto).toList(),
//                lessons.getNumber(),
//                lessons.getSize(),
//                lessons.getTotalElements(),
//                lessons.getTotalPages(),
//                lessons.isLast(),
//                lessons.isFirst(),
//                lessons.getNumberOfElements(),
//                lessons.isEmpty()
//        );
//    }

    @Override
    public LessonResponseDTO read(Long id) {
        return lessonRepository.findById(id).
                map(lessonMapper::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Lesson not found with id: " + id));

    }

    @Transactional
    @Override
    public LessonResponseDTO create(LessonCreatDTO lessonCreatDTO) {

        if (lessonRepository.existsByTitleAndModuleId(lessonCreatDTO.getTitle(), lessonCreatDTO.getModuleId())) {
            throw new DataConflictException(
                    "Lesson with title '" + lessonCreatDTO.getTitle() + "' already exists in this module."
            );
        }

        Module module = moduleRepository.findById(lessonCreatDTO.getModuleId())
                .orElseThrow(() -> new EntityNotFoundException("Module not found with id: " + lessonCreatDTO.getModuleId()));

        Lesson lesson = new Lesson();
        lesson.setTitle(lessonCreatDTO.getTitle());
        lesson.setContent(lessonCreatDTO.getContent());
        lesson.setModule(module);
        lesson.setOrderIndex(module.getLessons().size());
        lesson.setFree(lessonCreatDTO.isFree());

        Lesson saveLesson = lessonRepository.save(lesson);

        return lessonMapper.toDTO(saveLesson);

    }

    @Transactional
    @Override
    public LessonResponseDTO update(Long id, LessonUpdateDTO lessonUpdateDTO) {
        Lesson lesson = lessonRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Lesson not found with id: " + id));

        if (lessonRepository.existsByTitleAndModuleIdAndIdNot(lessonUpdateDTO.getTitle(), lesson.getModule().getId(), id)) {
            throw new DataConflictException(
                    "Another lesson with title '" + lessonUpdateDTO.getTitle() + "' already exists in this module."
            );
        }

        lesson.setTitle(lessonUpdateDTO.getTitle());
        lesson.setContent(lessonUpdateDTO.getContent());
        lesson.setFree(lessonUpdateDTO.isFree());

        Lesson saveLesson = lessonRepository.save(lesson);

        return lessonMapper.toDTO(saveLesson);
    }

    @Transactional
    @Override
    public void delete(Long id) {

        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Lesson not found with id: " + id));

        Long moduleId = lesson.getModule().getId();
        Integer orderIndexOfDeleted = lesson.getOrderIndex();

        lessonRepository.delete(lesson);

        lessonRepository.flush();

        lessonRepository.shiftOrderIndexesAfterDelete(moduleId, orderIndexOfDeleted);

    }

    @Override
    @Transactional
    public void updateOrder(Long moduleId, List<Long> orderedLessonIds) {
        // 1. Modulga tegishli barcha darslarni bazadan bitta so'rov bilan olamiz.
        List<Lesson> lessonsInDb = lessonRepository.findAllByModuleId(moduleId);

        // 2. O'lchamlar mosligini tekshiramiz. Bu frontend xatosining oldini oladi.
        if (orderedLessonIds.size() != lessonsInDb.size()) {
            throw new DataConflictException("The number of sent IDs does not match the number of lessons in the module.");
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

    /**
     * @param username String
     * @param lessonId Long
     * @return boolean
     */
    @Override
    public boolean isPaymentOrFreeLesson(String username, Long lessonId) {

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Lesson not found with id: " + lessonId));

        if (lesson.isFree())
            return true;

        Module module = lesson.getModule();
        module.getPayments().stream()
                .filter(payment -> payment.getUser().getUsername().equals(username))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Tolov qilinmagan with username: " + username));

        return true;
    }
}
