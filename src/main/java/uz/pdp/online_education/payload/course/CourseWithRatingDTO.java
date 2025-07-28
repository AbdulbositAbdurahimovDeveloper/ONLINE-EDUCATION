package uz.pdp.online_education.payload.course;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.online_education.model.Course;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseWithRatingDTO {
    private Course course;
    private Double averageRating;
}