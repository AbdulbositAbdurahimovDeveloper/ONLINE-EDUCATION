package uz.pdp.online_education.service.interfaces;

import org.springframework.data.domain.Page;
import uz.pdp.online_education.model.Module;

public interface ModuleService {
    Page<Module> read(Long courseId, Integer page, Integer size);
}
