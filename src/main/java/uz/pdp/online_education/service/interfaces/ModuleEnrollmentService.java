package uz.pdp.online_education.service.interfaces;

import uz.pdp.online_education.payload.enrollment.ModuleEnrollmentRequestDTO;
import uz.pdp.online_education.payload.enrollment.ModuleEnrollmentResponseDTO;

import java.util.List;

public interface ModuleEnrollmentService {


    ModuleEnrollmentResponseDTO enrollUser(ModuleEnrollmentRequestDTO requestDTO);

    List<ModuleEnrollmentResponseDTO> getEnrollmentsByUser(Long userId);

    List<ModuleEnrollmentResponseDTO> getEnrollmentsByModule(Long moduleId);

    void unenrollUser(Long enrollmentId);
}
