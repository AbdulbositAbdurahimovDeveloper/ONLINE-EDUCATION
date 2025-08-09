package uz.pdp.online_education.telegram.model;

import jakarta.persistence.*;
import lombok.*;
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

    //    @OneToOne
//    private User user;
    @OneToOne// FetchType.LAZY tavsiya etiladi
    @JoinColumn(name = "user_id", unique = true)
    @ToString.Exclude // Bu ustun aloqani o'rnatadi
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserState userState;

}
