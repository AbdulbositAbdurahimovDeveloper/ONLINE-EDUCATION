package uz.pdp.online_education.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.online_education.model.Module;

public interface ModuleRepository extends JpaRepository<Module, Long> {
}