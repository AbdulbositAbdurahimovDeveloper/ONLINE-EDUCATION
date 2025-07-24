package uz.pdp.online_education.service;

import org.springframework.data.domain.Page;
import uz.pdp.online_education.model.Module;
import uz.pdp.online_education.payload.module.ModuleCreateDTO;
import uz.pdp.online_education.payload.module.ModuleDetailDTO;
import uz.pdp.online_education.payload.module.ModuleUpdateDTO;

public interface ModuleService {
    Page<Module> read(Long courseId, Integer page, Integer size);

    ModuleDetailDTO read(Long id);

    ModuleDetailDTO create(ModuleCreateDTO moduleCreateDTO);

    ModuleDetailDTO update(Long id, ModuleUpdateDTO moduleUpdateDTO);

    void delete(Long id);

    boolean isUserEnrolled(String username, Long moduleId);
}
