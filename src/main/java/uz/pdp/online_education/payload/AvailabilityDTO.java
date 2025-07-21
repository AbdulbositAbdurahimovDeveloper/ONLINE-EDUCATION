package uz.pdp.online_education.payload;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AvailabilityDTO {
    private boolean available;
}