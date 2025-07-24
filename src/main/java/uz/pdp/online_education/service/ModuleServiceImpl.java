package uz.pdp.online_education.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.mapper.ModuleMapper;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.model.Module;
import uz.pdp.online_education.model.Payment;
import uz.pdp.online_education.payload.module.ModuleCreateDTO;
import uz.pdp.online_education.payload.module.ModuleDetailDTO;
import uz.pdp.online_education.payload.module.ModuleUpdateDTO;
import uz.pdp.online_education.repository.CourseRepository;
import uz.pdp.online_education.repository.ModuleRepository;
import uz.pdp.online_education.service.interfaces.ModuleService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ModuleServiceImpl implements ModuleService {

    private final ModuleRepository moduleRepository;
    private final ModuleMapper moduleMapper;
    private final CourseRepository courseRepository;

    /**
     * @param courseId Long
     * @param page Integer
     * @param size Integer
     * @return Page
     */
    @Override
    public Page<Module> read(Long courseId, Integer page, Integer size) {
        Sort sort = Sort.by(Sort.Direction.ASC, Module.Fields.orderIndex);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        return moduleRepository.findByCourseId(courseId, pageRequest);
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


        return null;
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
        module.setOrderIndex(module.getOrderIndex());

        moduleRepository.save(module);

        return moduleMapper.toModuleDetailsDTO(module);
    }

    /**
     * @param id
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
                ).findFirst().orElseThrow(()-> new BadCredentialsException("Siz ushbu moduleni korishingiz uchun avval tolov qiling"));

        return true;
    }
}
