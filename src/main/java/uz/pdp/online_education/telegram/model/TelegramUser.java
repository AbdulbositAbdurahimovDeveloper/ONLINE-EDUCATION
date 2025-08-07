package uz.pdp.online_education.telegram.model;

import jakarta.persistence.*;
import lombok.*;
import uz.pdp.online_education.enums.Role;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.telegram.enums.UserState;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity(name = "telegram_users")
public class TelegramUser {

    @Id
    private Long chatId;

    @OneToOne
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserState userState;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,name = "selected_role")
    private Role currentRole;


}
