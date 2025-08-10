package uz.pdp.online_education.service.interfaces;

import uz.pdp.online_education.payload.enrollment.ModuleEnrollmentRequestDTO;
import uz.pdp.online_education.payload.enrollment.ModuleEnrollmentResponseDTO;

import java.util.List;

public interface ModuleEnrollmentService {

    // So'rov uchun alohida, javob uchun alohida DTO
    ModuleEnrollmentResponseDTO enrollUser(ModuleEnrollmentRequestDTO requestDTO);

    List<ModuleEnrollmentResponseDTO> getEnrollmentsByUser(Long userId);

    List<ModuleEnrollmentResponseDTO> getEnrollmentsByModule(Long moduleId);

    void unenrollUser(Long enrollmentId);
}
