package uz.pdp.online_education.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.online_education.model.Course;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findAllByCategoryId(Long categoryId);
    List<Course> findAllByInstructorId(Long instructorId);
}
