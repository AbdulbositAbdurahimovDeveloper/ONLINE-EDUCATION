package uz.pdp.online_education.config;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import uz.pdp.online_education.enums.Role;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.model.UserProfile;
import uz.pdp.online_education.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        // Agar bazada foydalanuvchilar yo'q bo'lsa, boshlang'ich ma'lumotlarni yaratamiz
        if (userRepository.count() == 0) {
            log.info("❗\uFE0F❗\uFE0F❗\uFE0F");
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

            log.info("❗\uFE0F❗\uFE0F❗\uFE0F❗\uFE0F");
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