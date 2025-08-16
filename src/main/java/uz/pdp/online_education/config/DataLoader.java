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
            TelegramUser telegramUser = new TelegramUser(
                    chatId,
                    user,
                    UserState.DEFAULT
            );
            telegramUserRepository.save(telegramUser);
        }


    }

    private void createBaseUser() {
        // Agar bazada foydalanuvchilar yo'q bo'lsa, boshlang'ich ma'lumotlarni yaratamiz
        if (userRepository.count() == 0) {
            log.info("❗️❗️❗️");
            log.info("Bazada foydalanuvchilar yo'q. Boshlang'ich ma'lumotlar yaratilmoqda...");

            // ===============================================
            //          3 XIL ROLDA FOYDALANUVCHI YARATISH
            // ===============================================

            // --- 1-ROL: ADMIN YARATILMOQDA ---
            createUser(
                    "admin",                 // username
                    Role.ADMIN,              // role
                    "Admin", "Boshqaruvchi",  // firstName, lastName
                    "admin@pdp.uz",          // email
                    "+998991231212"
            );

            // --- 2-ROL: INSTRUCTOR (O'QITUVCHI) YARATILMOQDA ---
            createUser(
                    "instructor",            // username
                    Role.INSTRUCTOR,         // role
                    "John", "Doe",           // firstName, lastName
                    "instructor@pdp.uz",      // email
                    "+998991231111"
            );

            // --- 3-ROL: STUDENT (TALABA) YARATILMOQDA ---
            createUser(
                    "student",               // username
                    Role.STUDENT,            // role
                    "Alice",
                    "Smith",        // firstName, lastName
                    "student@pdp.uz",         // email
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
        // Yangi User obyektini yaratamiz
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("123")); // Parolni xavfsiz holatda saqlash SHART!
        user.setRole(role);

        // Yangi UserProfile obyektini yaratamiz
        UserProfile userProfile = new UserProfile();
        userProfile.setFirstName(firstName);
        userProfile.setLastName(lastName);
        userProfile.setEmail(email);
        userProfile.setPhoneNumber(phoneNumber);

        if (role == Role.INSTRUCTOR) {
            userProfile.setBio("Tajribali Java va Spring Framework o'qituvchisi.");
        }

        // User va UserProfile'ni bir-biriga bog'laymiz
        user.setProfile(userProfile);
        userProfile.setUser(user);

        // User'ni saqlaymiz (Cascade tufayli UserProfile ham avtomatik saqlanadi)
        userRepository.save(user);
    }
}