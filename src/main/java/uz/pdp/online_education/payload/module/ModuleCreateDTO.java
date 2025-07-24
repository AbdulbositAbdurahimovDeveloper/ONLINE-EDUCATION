package uz.pdp.online_education.payload.module;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModuleCreateDTO {

    @NotBlank(message = "Modul sarlavhasi bo'sh bo'lishi mumkin emas")
    @Size(min = 3, max = 200, message = "Sarlavha 3 dan 200 gacha belgidan iborat bo'lishi kerak")
    private String title;

    @Size(max = 1000, message = "Tavsif 1000 ta belgidan oshmasligi kerak")
    private String description;
    
    @NotNull(message = "Modul narxi ko'rsatilishi shart")
    @Min(value = 0, message = "Narx manfiy bo'lishi mumkin emas")
    private Long price;

    @NotNull(message = "Tartib raqami ko'rsatilishi shart")
    @Min(value = 1, message = "Tartib raqami 1 dan boshlanishi kerak")
    private Integer orderIndex;

    @NotNull(message = "Kurs ID'si ko'rsatilishi shart")
    private Long courseId;
}