package uz.pdp.online_education.payload.module;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModuleUpdateDTO {

    @Size(min = 3, max = 200, message = "Sarlavha 3 dan 200 gacha belgidan iborat bo'lishi kerak")
    private String title;

    @Size(max = 1000, message = "Tavsif 1000 ta belgidan oshmasligi kerak")
    private String description;

    @Min(value = 0, message = "Narx manfiy bo'lishi mumkin emas")
    private Long price;

    @Min(value = 1, message = "Tartib raqami 1 dan boshlanishi kerak")
    private Integer orderIndex;
}