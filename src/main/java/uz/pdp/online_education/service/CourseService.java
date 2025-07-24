package uz.pdp.online_education.service;

import org.springframework.data.domain.Page;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.payload.course.CourseCreateDTO;
import uz.pdp.online_education.payload.course.CourseDetailDTO;
import uz.pdp.online_education.payload.course.CourseUpdateDTO;

public interface CourseService {
    Page<Course> read(Integer page, Integer size);

    CourseDetailDTO read(Long id);

    CourseDetailDTO create(CourseCreateDTO courseCreateDTO, User instructor);

    CourseDetailDTO update(Long id, CourseUpdateDTO courseUpdateDTO, User instructor);

    void delete(Long id);
}
