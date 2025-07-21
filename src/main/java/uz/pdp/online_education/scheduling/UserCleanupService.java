package uz.pdp.online_education.scheduling;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.model.VerificationToken;
import uz.pdp.online_education.repository.VerificationTokenRepository;
import uz.pdp.online_education.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserCleanupService {

    private final VerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;

    /**
     * Bu metod har kuni yarim tunda ishga tushadi (cron = "sekund minut soat kun oy hafta_kuni").
     * U 24 soatdan ko'p vaqt oldin yaratilgan va hali tasdiqlanmagan
     * foydalanuvchilarni o'chirib yuboradi.
     */
    @Scheduled(cron = "0 0 0 * * *") // Har kuni 00:00:00 da
    @Transactional
    public void cleanupUnverifiedUsers() {
        System.out.println("Running unverified user cleanup task...");
        
        // Amal qilish muddati tugagan tokenlarni topamiz
        var expiredTokens = tokenRepository.findAllByExpiryDateBefore(LocalDateTime.now());

        for (VerificationToken token : expiredTokens) {
            User user = token.getUser();
            // Foydalanuvchi hali ham faollashtirilmaganini tekshiramiz
            if (user != null && !user.isEnabled()) {
                // Tokenni va unga bog'liq foydalanuvchini o'chiramiz
                // Cascade sozlamalaringizga qarab, faqat tokenni o'chirish yetarli bo'lishi mumkin.
                // Ishonchli bo'lishi uchun ikkalasini ham o'chiramiz.
                tokenRepository.delete(token);
                userRepository.delete(user);
                System.out.println("Deleted unverified user: " + user.getUsername());
            } else if (user != null){
                // Agar user aktivlashgan bo'lsa, ortiqcha token kerak emas
                tokenRepository.delete(token);
            }
        }
    }
}