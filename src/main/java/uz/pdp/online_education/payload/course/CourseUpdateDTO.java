package uz.pdp.online_education.payload.course;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseUpdateDTO implements Serializable {

    @Size(min = 5, max = 150, message = "Sarlavha 5 dan 150 gacha belgidan iborat bo'lishi kerak")
    private String title;

    @Size(max = 1000, message = "Tavsif 1000 ta belgidan oshmasligi kerak")
    private String description;

    private Long thumbnailId;

//    private Long instructorId;

    private Long categoryId;
}