package uz.pdp.online_education.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.online_education.model.Module;

import java.util.List;

public interface ModuleRepository extends JpaRepository<Module, Long> {
    Page<Module> findByCourseId(Long courseId, Pageable pageable);
}