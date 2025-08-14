package uz.pdp.online_education.payload.comment;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseDTO {
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
