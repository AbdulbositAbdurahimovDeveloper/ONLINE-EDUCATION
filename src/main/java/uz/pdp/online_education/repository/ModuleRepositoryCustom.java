package uz.pdp.online_education.repository;

import uz.pdp.online_education.payload.UserModuleStatusDTO;

import java.util.List;

public interface ModuleRepositoryCustom {
    List<UserModuleStatusDTO> findUserModuleStatusesInCourse(Long userId, Long courseId);
}