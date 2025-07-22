package uz.pdp.online_education.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.online_education.model.Category;

/**
 * Created by: suhrob
 */

public interface CategoryRepository extends JpaRepository<Category, Long> {
}