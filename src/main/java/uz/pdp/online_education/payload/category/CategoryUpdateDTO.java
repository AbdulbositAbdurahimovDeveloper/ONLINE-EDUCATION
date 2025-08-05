package uz.pdp.online_education.payload.category;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryUpdateDTO {
    @NotBlank
    private String name;
    private String icon;
}
