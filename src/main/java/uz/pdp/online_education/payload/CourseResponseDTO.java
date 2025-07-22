package uz.pdp.online_education.payload;

import lombok.Data;

@Data
public class CourseResponseDTO {

    private Long id;

    private String title;

    private String description;

    private String slug;

    private String instructorFullName;

    private String categoryName;
}
