package uz.pdp.online_education.telegram.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.telegram.enums.UserState;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
@Entity(name = "telegram_users")
public class TelegramUser {

    @Id
    private Long chatId;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    @JsonBackReference
    @ToString.Exclude
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserState userState;

    @Lob
    @Column(name = "temporary_data")
    private String temporaryData;

}
