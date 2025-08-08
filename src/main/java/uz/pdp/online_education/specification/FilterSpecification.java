package uz.pdp.online_education.specification;

import org.springframework.data.jpa.domain.Specification;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.payload.FilterDTO;
import uz.pdp.online_education.payload.course.CourseDetailDTO;

public class FilterSpecification {

    public static Specification<Course> filterBy(FilterDTO filterDTO){
        return null;
    }
}
