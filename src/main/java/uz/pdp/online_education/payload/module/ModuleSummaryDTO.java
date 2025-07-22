package uz.pdp.online_education.payload.module;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleSummaryDTO {
    private Long id;
    private String title;
    private Long price;
    private Integer orderIndex;
    private Integer lessonsCount; // Bu modulda nechta dars borligi
}