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
import uz.pdp.online_education.mapper.ModuleMapper;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.model.Module;
import uz.pdp.online_education.payload.PageDTO;
import uz.pdp.online_education.payload.module.ModuleCreateDTO;
import uz.pdp.online_education.payload.module.ModuleDetailDTO;
import uz.pdp.online_education.payload.module.ModuleUpdateDTO;
import uz.pdp.online_education.repository.CourseRepository;
import uz.pdp.online_education.repository.ModuleRepository;
import uz.pdp.online_education.service.interfaces.ModuleService;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModuleServiceImpl implements ModuleService {

    private final ModuleRepository moduleRepository;
    private final ModuleMapper moduleMapper;
    private final CourseRepository courseRepository;

    /**
     * @param courseId Long
     * @param page     Integer
     * @param size     Integer
     * @return Page
     */
    @Override
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
    public ModuleDetailDTO read(Long id) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Module nor found with id: " + id));

        return moduleMapper.toModuleDetailsDTO(module);
    }

    /**
     * @param moduleCreateDTO Module
     * @return moduleDetailsDTO
     */
    @Override
    public ModuleDetailDTO create(ModuleCreateDTO moduleCreateDTO) {

        Course course = courseRepository.findById(moduleCreateDTO.getCourseId())
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + moduleCreateDTO.getCourseId()));

        Module module = new Module();
        module.setTitle(moduleCreateDTO.getTitle());
        module.setDescription(moduleCreateDTO.getDescription());
        module.setPrice(moduleCreateDTO.getPrice());
        module.setOrderIndex(module.getOrderIndex());
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
    public ModuleDetailDTO update(Long id, ModuleUpdateDTO moduleUpdateDTO) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Module nor found with id: " + id));

        module.setTitle(moduleUpdateDTO.getTitle());
        module.setDescription(moduleUpdateDTO.getDescription());
        module.setPrice(moduleUpdateDTO.getPrice());

        moduleRepository.save(module);

        return moduleMapper.toModuleDetailsDTO(module);
    }

    /**
     * @param courseId         Long
     * @param orderedModuleIds List
     */
    @Override
    @Transactional // Bu annotatsiya juda muhim! Operatsiya atomar bo'lishini ta'minlaydi.
    public void updateModuleOrderIndex(Long courseId, List<Long> orderedModuleIds) {

        // 1. Kursga tegishli barcha modullarni bazadan olamiz.
        // Sizning kodingiz:
        List<Module> modulesInDb = moduleRepository.findAllByCourse_Id(courseId);

        // 2. O'lchamlar mosligini tekshiramiz.
        // Sizning kodingiz:
        if (orderedModuleIds.size() != modulesInDb.size()) {
            // DataConflictException o'rniga BadRequestException to'g'riroq keladi,
            // chunki bu klientning noto'g'ri ma'lumot yuborganini bildiradi.
            throw new DataConflictException("The number of module IDs sent does not match the number of modules in the course.");
        }

        // --- KODNING DAVOMI (ENG MUHIM QISMLAR) ---

        // 3. Modullarni tez topish uchun Map'ga joylashtiramiz.
        // Kalit - modulning IDsi, qiymat - modulning o'zi.
        Map<Long, Module> moduleMap = modulesInDb.stream()
                .collect(Collectors.toMap(Module::getId, Function.identity()));

        // 4. Ma'lumotlar yaxlitligini tekshiramiz: Frontend yuborgan barcha IDlar haqiqatdan ham
        // shu kursga tegishlimi va takrorlanmasmi?
        // Bu bazadan olingan IDlar to'plami bilan Frontend yuborgan IDlar to'plami bir xil ekanligini
        // tekshirish orqali oson amalga oshiriladi.
        if (!moduleMap.keySet().equals(new HashSet<>(orderedModuleIds))) {
            throw new DataConflictException("The provided module IDs are invalid, do not match the course's modules, or contain duplicates.");
        }

        // 5. Yangi tartib raqamlarini ('orderIndex') o'rnatamiz.
        // Bu eng sodda va xavfsiz qism. Indekslar serverda, tsikl yordamida generatsiya qilinadi.
        for (int i = 0; i < orderedModuleIds.size(); i++) {
            Long moduleId = orderedModuleIds.get(i);
            Module moduleToUpdate = moduleMap.get(moduleId);

            // moduleToUpdate hech qachon null bo'lmaydi, chunki yuqorida tekshirdik.
            moduleToUpdate.setOrderIndex(i); // Yangi tartib raqami = tsikldagi o'rin (0, 1, 2, ...)
        }

        // 6. O'zgarishlarni ma'lumotlar bazasiga saqlaymiz.
        // JPA/Hibernate aqlli ishlaydi va odatda faqat o'zgargan (dirty) entity'lar
        // uchun UPDATE so'rovlarini generatsiya qiladi.
        moduleRepository.saveAll(modulesInDb);
    }
//    @Override
//    public void updateModuleOrderIndex(Long courseId, List<Long> orderedModules) {
//
//        List<Module> allByCourseId = moduleRepository.findAllByCourse_Id(courseId);
//
//        if (orderedModules.size() != allByCourseId.size()) {
//            throw new DataConflictException("The number of module IDs sent does not match the number of modules in the course.");
//        }
//
//
//
//    }

    /**
     * @param id Long
     */
    @Override
    public void delete(Long id) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Module nor found with id: " + id));

        moduleRepository.delete(module);
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
}
