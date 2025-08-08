package uz.pdp.online_education.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uz.pdp.online_education.payload.FilterDTO;
import uz.pdp.online_education.payload.course.CourseWithRatingDTO;

public interface CourseRepositoryCustom {
    Page<CourseWithRatingDTO> filterWithCriteria(FilterDTO filter, Pageable pageable);
}