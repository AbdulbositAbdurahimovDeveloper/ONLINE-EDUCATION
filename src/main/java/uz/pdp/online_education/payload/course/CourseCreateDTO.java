package uz.pdp.online_education.payload.course;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseCreateDTO implements Serializable {

    @NotBlank(message = "Kurs sarlavhasi bo'sh bo'lishi mumkin emas")
    @Size(min = 5, max = 150, message = "Sarlavha 5 dan 150 gacha belgidan iborat bo'lishi kerak")
    private String title;

    @Size(max = 1000, message = "Tavsif 1000 ta belgidan oshmasligi kerak")
    private String description;

    @NotNull(message = "Ikonka ID'si ko'rsatilishi shart")
    private Long thumbnailId; // <<< Faqat ID

//    @NotNull(message = "O'qituvchi ID'si ko'rsatilishi shart")
//    private Long instructorId; // <<< Faqat ID

    @NotNull(message = "Kategoriya ID'si ko'rsatilishi shart")
    private Long categoryId; // <<< Faqat ID
}