package uz.pdp.online_education.payload.category;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryCreateDTO {
    private String name;
    private String icon;
}
