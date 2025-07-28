package uz.pdp.online_education.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.N;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class FilterDTO {

    private String search;

    private List<String> categoryTitle;

    private List<String> instructorName;

    private Double fromPrice;

    private Double toPrice;

    private Integer review;

}
