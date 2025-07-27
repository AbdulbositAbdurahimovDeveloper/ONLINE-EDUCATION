package uz.pdp.online_education.payload.content;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for {@link uz.pdp.online_education.model.lesson.Content}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContentUpdateOrderRequestDTO implements Serializable {

    @NotEmpty
    private List<Long> contentIds;

}