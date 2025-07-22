package uz.pdp.online_education.payload;

import lombok.Data;

@Data
public class CourseRequestDTO {

    private String title;

    private String description;

    private String slug;

    private Long thumbnailId;

    private Long instructorId;

    private Long categoryId;
}
