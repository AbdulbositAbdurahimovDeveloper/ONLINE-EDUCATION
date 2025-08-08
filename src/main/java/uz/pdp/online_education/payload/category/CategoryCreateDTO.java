package uz.pdp.online_education.payload.category;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryCreateDTO {
    @NotBlank
    private String name;
    @NotBlank
    private String icon;
}
