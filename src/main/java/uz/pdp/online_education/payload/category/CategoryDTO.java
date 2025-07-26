package uz.pdp.online_education.payload.category;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link uz.pdp.online_education.model.Category}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDTO implements Serializable {

    private Long id;
    private String name;
    private String icon;
    private String slug;
    private Long createdAt;
    private Long updatedAt;
}