package uz.pdp.online_education.payload;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ModuleOrderIndexDTO {

    @NotNull
    @Min(0)
    private Long moduleId;
}
