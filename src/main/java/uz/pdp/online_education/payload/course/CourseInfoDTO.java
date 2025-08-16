package uz.pdp.online_education.payload.course;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.N;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CourseInfoDTO {
    private Long id;
    private String title;
    private String description;
    private String categoryName;
    private String instructorName;
    private int moduleCount;
    private LocalDateTime createdAt;
    private boolean isDeleted;
}


