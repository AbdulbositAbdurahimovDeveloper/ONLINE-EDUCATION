package uz.pdp.online_education.payload.course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ModuleSummaryDTO {
    private Long id;
    private String title;
    private Integer order; // Modulning tartib raqami
    private Integer lessonsCount;
}