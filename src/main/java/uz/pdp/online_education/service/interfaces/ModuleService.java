package uz.pdp.online_education.service.interfaces;

import uz.pdp.online_education.payload.ModuleOrderIndexDTO;
import uz.pdp.online_education.payload.PageDTO;
import uz.pdp.online_education.payload.lesson.LessonResponseDTO;
import uz.pdp.online_education.payload.module.ModuleCreateDTO;
import uz.pdp.online_education.payload.module.ModuleDetailDTO;
import uz.pdp.online_education.payload.module.ModuleUpdateDTO;

import java.util.List;

public interface ModuleService {
    PageDTO<ModuleDetailDTO> read(Long courseId, Integer page, Integer size);

    ModuleDetailDTO read(Long id);

    PageDTO<LessonResponseDTO> readLessons(Long id, Integer page, Integer size);

    ModuleDetailDTO create(ModuleCreateDTO moduleCreateDTO);

    ModuleDetailDTO update(Long id, ModuleUpdateDTO moduleUpdateDTO);

    void updateModuleOrderIndex(Long courseId, List<ModuleOrderIndexDTO> moduleUpdateDTO);

    void delete(Long id);

    boolean isUserEnrolled(String username, Long moduleId);
}
