package uz.pdp.online_education.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.online_education.enums.Role;
import uz.pdp.online_education.exceptions.DataConflictException;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.model.UserProfile;
import uz.pdp.online_education.payload.user.UserRegisterRequestDTO;
import uz.pdp.online_education.repository.TelegramUserRepository; // Kerakli import
import uz.pdp.online_education.repository.UserProfileRepository;
import uz.pdp.online_education.repository.UserRepository;
import uz.pdp.online_education.telegram.enums.UserState;
import uz.pdp.online_education.telegram.model.TelegramUser;

// ... boshqa importlar

@Service
@RequiredArgsConstructor // Lombok
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final TelegramUserRepository telegramUserRepository;

    /**
     * Registers a new user coming from the Telegram bot and links their account immediately.
     * Skips email verification and sets the account as enabled.
     *
     * @param request The DTO containing registration data (firstName, lastName, etc.).
     * @param chatId  The user's unique Telegram chat ID.
     */
    @Transactional
    @Override
    public void registerAndLinkTelegramAccount(UserRegisterRequestDTO request, Long chatId) {
        // 1. Username yoki email bandligini tekshirish (mavjud logikadan olingan)
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DataConflictException("Bu username allaqachon band!");
        }
        if (userProfileRepository.existsByEmail(request.getEmail())) {
            throw new DataConflictException("Bu email allaqachon ro'yxatdan o'tgan!");
        }
        // Telefon raqam uchun ham tekshiruv
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty() &&
                userProfileRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new DataConflictException("Bu telefon raqami allaqachon ro'yxatdan o'tgan!");
        }

        // 2. User obyektini yaratish
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.STUDENT);
        user.setEnabled(true); // <-- Eng muhim farq: Hisobni darhol aktiv qilish

        // 3. UserProfile obyektini yaratish
        UserProfile profile = new UserProfile();
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setEmail(request.getEmail());
        profile.setPhoneNumber(request.getPhoneNumber());
        // bio va profilePicture'ni DTO'dan olish mumkin
        profile.setBio(request.getBio());

        // 4. Ikkala obyektni bir-biriga bog'lash
        user.setProfile(profile);
        profile.setUser(user);

        // 5. User-ni saqlash (bu UserProfile'ni ham avtomatik saqlaydi)
        User savedUser = userRepository.save(user);

        // 6. ENG MUHIM QADAM: Telegram profilini yaratish va bog'lash
        // Avval bu chatId bilan boshqa profil bog'lanmaganligiga ishonch hosil qilish
        if (telegramUserRepository.getCurrentUser(chatId).getUser() != null) {
            throw new DataConflictException("Bu Telegram profili allaqachon boshqa akkauntga ulangan!");
        }

        TelegramUser telegramUser = new TelegramUser();
        telegramUser.setChatId(chatId);
        telegramUser.setUser(savedUser); // Yangi yaratilgan user bilan bog'lash
        telegramUser.setUserState(UserState.AUTHENTICATED); // Boshlang'ich holat

        telegramUserRepository.save(telegramUser);

        // Bu metodda hech narsa qaytarish shart emas. Agar xatolik bo'lsa, exception otiladi.
    }
}