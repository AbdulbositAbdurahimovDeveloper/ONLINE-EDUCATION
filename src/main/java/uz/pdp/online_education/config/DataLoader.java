package uz.pdp.online_education.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import uz.pdp.online_education.enums.Role;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.model.UserProfile;
import uz.pdp.online_education.repository.UserRepository;
import uz.pdp.online_education.telegram.enums.UserState;
import uz.pdp.online_education.telegram.model.TelegramUser;
import uz.pdp.online_education.repository.TelegramUserRepository;

@Slf4j
@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TelegramUserRepository telegramUserRepository;


//    @Value("${telegram.bot.chat-id}")
    private Long chatId;

    public DataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder, @Lazy TelegramUserRepository telegramUserRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.telegramUserRepository = telegramUserRepository;
    }

    @Override
    public void run(String... args) {

        createBaseUser();
//        createTgUser();

    }

    private void createTgUser() {

        User user = userRepository.findByUsername("admin").orElse(null);
        if (user != null) {
            TelegramUser telegramUser = TelegramUser.builder()
                    .chatId(chatId)
                    .user(user)
                    .userState(UserState.DEFAULT)
                    .build();
            telegramUserRepository.save(telegramUser);
        }


    }

    private void createBaseUser() {

        if (userRepository.count() == 0) {
            log.info("❗️❗️❗️");
            log.info("Bazada foydalanuvchilar yo'q. Boshlang'ich ma'lumotlar yaratilmoqda...");


            createUser(
                    "admin",
                    Role.ADMIN,
                    "Admin", "Boshqaruvchi",
                    "admin@pdp.uz",
                    "+998991231212"
            );


            createUser(
                    "instructor",
                    Role.INSTRUCTOR,
                    "John", "Doe",
                    "instructor@pdp.uz",
                    "+998991231111"
            );


            createUser(
                    "student",
                    Role.STUDENT,
                    "Alice",
                    "Smith",
                    "student@pdp.uz",
                    "+998991231234"
            );

            log.info("❗️❗️❗️❗️");
            log.info("Boshlang'ich ma'lumotlar muvaffaqiyatli yaratildi.");
        }
    }

    /**
     * Yangi foydalanuvchi va uning profilini yaratib, bazaga saqlovchi yordamchi metod.
     */
    private void createUser(String username, Role role, String firstName, String lastName, String email, String phoneNumber) {

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("123"));
        user.setRole(role);


        UserProfile userProfile = new UserProfile();
        userProfile.setFirstName(firstName);
        userProfile.setLastName(lastName);
        userProfile.setEmail(email);
        userProfile.setPhoneNumber(phoneNumber);

        if (role == Role.INSTRUCTOR) {
            userProfile.setBio("Tajribali Java va Spring Framework o'qituvchisi.");
        }


        user.setProfile(userProfile);
        userProfile.setUser(user);


        userRepository.save(user);
    }
}