package uz.pdp.online_education.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uz.pdp.online_education.model.Module;
import uz.pdp.online_education.repository.ModuleRepository;

@Service
@RequiredArgsConstructor
public class ModuleServiceImpl implements ModuleService {

    private final ModuleRepository moduleRepository;

    /**
     * @param courseId
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Module> read(Long courseId, Integer page, Integer size) {
        Sort sort = Sort.by(Sort.Direction.ASC, Module.Fields.orderIndex);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        return moduleRepository.findByCourseId(courseId, pageRequest);
    }
}
