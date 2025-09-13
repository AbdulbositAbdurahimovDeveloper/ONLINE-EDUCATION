package uz.pdp.online_education.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.online_education.exceptions.DataConflictException;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.mapper.LessonMapper;
import uz.pdp.online_education.mapper.ModuleMapper;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.model.Module;
import uz.pdp.online_education.model.lesson.AttachmentContent;
import uz.pdp.online_education.model.lesson.Content;
import uz.pdp.online_education.model.lesson.Lesson;
import uz.pdp.online_education.payload.ModuleOrderIndexDTO;
import uz.pdp.online_education.payload.PageDTO;
import uz.pdp.online_education.payload.lesson.LessonResponseDTO;
import uz.pdp.online_education.payload.module.ModuleCreateDTO;
import uz.pdp.online_education.payload.module.ModuleDetailDTO;
import uz.pdp.online_education.payload.module.ModuleUpdateDTO;
import uz.pdp.online_education.repository.*;
import uz.pdp.online_education.service.interfaces.ModuleService;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModuleServiceImpl implements ModuleService {

    private final ModuleRepository moduleRepository;
    private final ModuleMapper moduleMapper;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final LessonMapper lessonMapper;
    private final AttachmentContentRepository attachmentContentRepository;
    private final ContentRepository contentRepository;

    /**
     * @param courseId Long
     * @param page     Integer
     * @param size     Integer
     * @return Page
     */
    @Override
    @Transactional(readOnly = true)
    public PageDTO<ModuleDetailDTO> read(Long courseId, Integer page, Integer size) {
        Sort sort = Sort.by(Sort.Direction.ASC, Module.Fields.orderIndex);
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Page<Module> byCourseId = moduleRepository.findByCourseId(courseId, pageRequest);

        return new PageDTO<>(
                byCourseId.getContent().stream().map(moduleMapper::toModuleDetailsDTO).toList(),
                byCourseId.getNumber(),
                byCourseId.getSize(),
                byCourseId.getTotalElements(),
                byCourseId.getTotalPages(),
                byCourseId.isLast(),
                byCourseId.isFirst(),
                byCourseId.getNumberOfElements(),
                byCourseId.isEmpty()
        );

    }

    /**
     * @param id Long
     * @return moduleDetailDTO
     */
    @Override
    @Transactional(readOnly = true)
    public ModuleDetailDTO read(Long id) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Module nor found with id: " + id));

        return moduleMapper.toModuleDetailsDTO(module);
    }

    /**
     * @param id
     * @param page
     * @param size
     * @return
     */
    @Override
    @Transactional(readOnly = true)
    public PageDTO<LessonResponseDTO> readLessons(Long id, Integer page, Integer size) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Module nor found with id: " + id));
        Sort sort = Sort.by(Sort.Direction.ASC, Lesson.Fields.orderIndex);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<Lesson> lessons = lessonRepository.findByModule_Id(id, pageRequest);


        return new PageDTO<>(
                lessons.getContent().stream().map(lessonMapper::toDTO).toList(),
                lessons.getNumber(),
                lessons.getSize(),
                lessons.getTotalElements(),
                lessons.getTotalPages(),
                lessons.isLast(),
                lessons.isFirst(),
                lessons.getNumberOfElements(),
                lessons.isEmpty()
        );
    }

    /**
     * @param moduleCreateDTO Module
     * @return moduleDetailsDTO
     */
    @Override
    @Transactional
    public ModuleDetailDTO create(ModuleCreateDTO moduleCreateDTO) {

        Course course = courseRepository.findById(moduleCreateDTO.getCourseId())
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + moduleCreateDTO.getCourseId()));

        if (moduleRepository.existsByTitle(moduleCreateDTO.getTitle()))
            throw new DataConflictException("Title already exists");

        Module module = new Module();
        module.setTitle(moduleCreateDTO.getTitle());
        module.setDescription(moduleCreateDTO.getDescription());
        module.setPrice(moduleCreateDTO.getPrice());
        int orderIndex = course.getModules() != null ? course.getModules().size() : 0;
        module.setOrderIndex(orderIndex);
        module.setCourse(course);

        moduleRepository.save(module);
        return moduleMapper.toModuleDetailsDTO(module);
    }

    /**
     * @param id              Long
     * @param moduleUpdateDTO Class
     * @return moduleDetailDTO
     */
    @Override
    @Transactional
    public ModuleDetailDTO update(Long id, ModuleUpdateDTO moduleUpdateDTO) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Module nor found with id: " + id));

        if (moduleUpdateDTO.getTitle() != null)
            module.setTitle(moduleUpdateDTO.getTitle());
        if (moduleUpdateDTO.getDescription() != null)
            module.setDescription(moduleUpdateDTO.getDescription());
        if (moduleUpdateDTO.getPrice() != null)
            module.setPrice(moduleUpdateDTO.getPrice());

        moduleRepository.save(module);

        return moduleMapper.toModuleDetailsDTO(module);
    }

    /**
     * @param courseId          Long
     * @param orderedModuleDtos List
     */
    @Override
    @Transactional
    public void updateModuleOrderIndex(Long courseId, List<ModuleOrderIndexDTO> orderedModuleDtos) {

        courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + courseId));


        List<Module> modulesInDb = moduleRepository.findAllByCourse_Id(courseId);


        List<Long> orderedModuleIds = orderedModuleDtos.stream()
                .map(ModuleOrderIndexDTO::getModuleId)
                .toList();


        if (orderedModuleIds.size() != modulesInDb.size()) {
            throw new DataConflictException("The number of module IDs sent does not match the number of modules in the course.");
        }

        Map<Long, Module> moduleMap = modulesInDb.stream()
                .collect(Collectors.toMap(Module::getId, Function.identity()));


        if (!moduleMap.keySet().equals(new HashSet<>(orderedModuleIds))) {
            throw new DataConflictException("The provided module IDs are invalid, do not match the course's modules, or contain duplicates.");
        }

        for (int i = 0; i < orderedModuleIds.size(); i++) {
            Long moduleId = orderedModuleIds.get(i);
            Module moduleToUpdate = moduleMap.get(moduleId);
            moduleToUpdate.setOrderIndex(i);
        }

        moduleRepository.saveAll(modulesInDb);
    }


    /**
     * @param id Long
     */
    @Override
    @Transactional
    public void delete(Long id) {
        Module moduleToDelete = moduleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Module not found with id: " + id));

        Course course = moduleToDelete.getCourse();
        Integer deletedOrderIndex = moduleToDelete.getOrderIndex();

        moduleToDelete.setOrderIndex(null);
        moduleRepository.saveAndFlush(moduleToDelete);

        moduleRepository.shiftOrderIndexesAfterDelete(course, deletedOrderIndex);


        course.getModules().remove(moduleToDelete);

    }

    /**
     * @param username String
     * @param moduleId Long
     * @return boolean
     */
    @Override
    public boolean isUserEnrolled(String username, Long moduleId) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new EntityNotFoundException("Module nor found with id: " + moduleId));

        module.getPayments().stream()
                .filter(payment -> payment.getUser().getUsername().equals(username)
                ).findFirst().orElseThrow(() -> new BadCredentialsException("Siz ushbu moduleni korishingiz uchun avval tolov qiling"));

        return true;
    }

    @Override
    public boolean isUserModuleBought(String username, Long attachmentContentId) {
        Content content = contentRepository.findById(attachmentContentId)
                .orElseThrow(() -> new EntityNotFoundException("Attachment content not found with id: " + attachmentContentId));


        Lesson lesson = content.getLesson();

        if (lesson.isFree())
            return true;

        Module module = lesson.getModule();

        module.getPayments().stream().filter(Payment -> Payment.getUser().getUsername().equals(username))
                .findFirst()
                .orElseThrow(() -> new BadCredentialsException("Siz ushbu videoni korishingiz uchun avval tolov qiling"));

        return true;
    }
}
