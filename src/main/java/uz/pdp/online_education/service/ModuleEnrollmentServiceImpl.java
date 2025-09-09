package uz.pdp.online_education.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.online_education.exceptions.DataConflictException;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.model.Module;
import uz.pdp.online_education.model.ModuleEnrollment;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.payload.enrollment.ModuleEnrollmentRequestDTO;
import uz.pdp.online_education.payload.enrollment.ModuleEnrollmentResponseDTO;
import uz.pdp.online_education.repository.ModuleEnrollmentRepository;
import uz.pdp.online_education.repository.ModuleRepository;
import uz.pdp.online_education.repository.UserRepository;
import uz.pdp.online_education.service.interfaces.ModuleEnrollmentService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Concrete implementation of the {@link ModuleEnrollmentService}.
 * Handles the business logic for enrolling users in modules.
 */
@Service
@RequiredArgsConstructor
public class ModuleEnrollmentServiceImpl implements ModuleEnrollmentService {

    private final ModuleEnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final ModuleRepository moduleRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public ModuleEnrollmentResponseDTO enrollUser(ModuleEnrollmentRequestDTO requestDTO) {
        Long userId = requestDTO.getUserId();
        Long moduleId = requestDTO.getModuleId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with id " + userId + " not found."));
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new EntityNotFoundException("Module with id " + moduleId + " not found."));

        if (enrollmentRepository.existsByUserIdAndModuleId(userId, moduleId)) {
            throw new DataConflictException("User is already enrolled in this module.");
        }

        ModuleEnrollment newEnrollment = new ModuleEnrollment();
        newEnrollment.setUser(user);
        newEnrollment.setModule(module);

        ModuleEnrollment savedEnrollment = enrollmentRepository.save(newEnrollment);

        return toResponseDTO(savedEnrollment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ModuleEnrollmentResponseDTO> getEnrollmentsByUser(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User with id " + userId + " not found.");
        }
        return enrollmentRepository.findAllByUserId(userId).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ModuleEnrollmentResponseDTO> getEnrollmentsByModule(Long moduleId) {

        if (!moduleRepository.existsById(moduleId)) {
            throw new EntityNotFoundException("Module with id " + moduleId + " not found.");
        }
        return enrollmentRepository.findAllByModuleId(moduleId).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unenrollUser(Long enrollmentId) {

        if (!enrollmentRepository.existsById(enrollmentId)) {
            throw new EntityNotFoundException("Enrollment with id " + enrollmentId + " not found.");
        }
        enrollmentRepository.deleteById(enrollmentId);
    }

    /**
     * A private helper method to map a {@link ModuleEnrollment} entity to a {@link ModuleEnrollmentResponseDTO}.
     * This encapsulates the mapping logic in one place.
     * @param enrollment The entity to map.
     * @return The corresponding DTO.
     */
    private ModuleEnrollmentResponseDTO toResponseDTO(ModuleEnrollment enrollment) {
        User user = enrollment.getUser();
        Module module = enrollment.getModule();
        Course course = module.getCourse();

        return new ModuleEnrollmentResponseDTO(
                enrollment.getId(),
                user.getId(),
                user.getProfile() != null ? user.getProfile().getFirstName() + " " + user.getProfile().getLastName() : "N/A",
                module.getId(),
                module.getTitle(),
                course != null ? course.getId() : null,
                course != null ? course.getTitle() : "N/A",
                enrollment.getProgressPercentage()
        );
    }
}