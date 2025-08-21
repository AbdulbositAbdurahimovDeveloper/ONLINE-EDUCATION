// NotificationService.java (yangi yoki o'zgartirilgan servis)
package uz.pdp.online_education.service; // Yoki sizdagi package

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.model.Review;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.service.interfaces.EmailService;
import uz.pdp.online_education.telegram.config.controller.OnlineEducationBot;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EmailService emailService; // HTML yubora oladigan qilib o'zgartirilishi kerak
    private final OnlineEducationBot onlineEducationBot; // Sizning bot klassingiz

    @Async // Operatsiyani asosiy oqimdan ajratadi, foydalanuvchi kutib qolmaydi
    @Transactional(readOnly = true)
    public void sendReviewNotification(Review review, boolean isNewReview) {
        // --- 1. KERAKLI MA'LUMOTLARNI YIG'ISH ---
        // Barcha ma'lumotni 'review' obyektidan olamiz. BU ENG MUHIM O'ZGARISH!
        Course course = review.getCourse();
        User instructor = course.getInstructor();
        User reviewer = review.getUser();

        if (instructor == null) {
            log.warn("Kursning instruktori topilmadi. Kurs ID: {}", course.getId());
            return;
        }

        // --- 2. XABARLARNI GENERATSIYA QILISH ---
        String subject = isNewReview ? "Yangi sharh qoldirildi!" : "Sharhingiz yangilandi!";
        String telegramMessage = buildTelegramMessage(review, reviewer, instructor, course, isNewReview);
        String emailBody = buildEmailHtmlBody(review, reviewer, instructor, course, isNewReview);

        // --- 3. EMAILGA YUBORISH ---
        if (instructor.getProfile() != null && instructor.getProfile().getEmail() != null) {
            try {
                // EmailService'da HTML yuborish uchun yangi metod kerak bo'ladi
                emailService.sendSimpleNotification(instructor.getProfile().getEmail(), subject, emailBody);
                log.info("Instruktorga email yuborildi: {}", instructor.getProfile().getEmail());
            } catch (Exception e) {
                log.error("Instruktorga email yuborishda xatolik: {}", e.getMessage());
            }
        }

        // --- 4. TELEGRAMGA YUBORISH ---
        if (instructor.getTelegramUser() != null && instructor.getTelegramUser().getChatId() != null) {
            try {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(instructor.getTelegramUser().getChatId().toString());
                sendMessage.setText(telegramMessage);
                sendMessage.setParseMode("HTML"); // HTML formatini yoqamiz
                onlineEducationBot.execute(sendMessage); // Bot'ning execute metodi
                log.info("Instruktorga telegram xabar yuborildi. Chat ID: {}", instructor.getTelegramUser().getChatId());
            } catch (Exception e) {
                log.error("Instruktorga telegram xabar yuborishda xatolik: {}", e.getMessage());
            }
        }
    }

    // Yordamchi metodlar (shu klass ichida private qilib yoziladi)
    private String getFullName(User user) {
        if (user.getProfile() != null && user.getProfile().getFirstName() != null) {
            return user.getProfile().getFirstName() + " " + user.getProfile().getLastName();
        }
        return user.getUsername();
    }

    private String generateStarRating(int rating) {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            stars.append(i < rating ? "⭐️" : "☆");
        }
        return stars.toString();
    }

    private String buildTelegramMessage(Review review, User reviewer, User instructor, Course course, boolean isNewReview) {
        String reviewType = isNewReview ? "yangi sharh" : "sharh yangilandi";
        String comment = review.getComment() != null && !review.getComment().isEmpty()
                ? "<blockquote>" + review.getComment() + "</blockquote>"
                : "<i>(Sharh matni qoldirilmagan)</i>";

        return String.format(
                "👋 Assalomu alaykum, <b>%s</b>!\n\n" +
                        "Sizning \"<b>%s</b>\" nomli kursingizga %s qoldirildi.\n\n" +
                        "👤 <b>Sharh muallifi:</b> %s\n" +
                        "⭐️ <b>Baho:</b> %s (%d/5)\n" +
                        "💬 <b>Izoh:</b>\n%s\n\n" +
                        "<i>Bu avtomatik tarzda yuborilgan xabar.</i>",
                getFullName(instructor),
                course.getTitle(),
                reviewType,
                getFullName(reviewer),
                generateStarRating(review.getRating()),
                review.getRating(),
                comment
        );
    }

    private String buildEmailHtmlBody(Review review, User reviewer, User instructor, Course course, boolean isNewReview) {
        String reviewType = isNewReview ? "Yangi sharh" : "Sharh yangilandi";
        String comment = review.getComment() != null && !review.getComment().isEmpty()
                ? review.getComment()
                : "(Sharh matni qoldirilmagan)";

        // Kursga link (saytingiz strukturasiga moslang)
        String courseLink = "https://sizning-saytingiz.uz/courses/" + course.getSlug();

        return "<!DOCTYPE html>"
                + "<html><head><style>"
                + "body{font-family: Arial, sans-serif; line-height: 1.6; color: #333;}"
                + ".container{width: 90%%; max-width: 600px; margin: 20px auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.05);}"
                + ".header{font-size: 24px; color: #444; margin-bottom: 20px; border-bottom: 2px solid #eee; padding-bottom: 10px;}"
                + ".content p{margin: 10px 0;}"
                + ".content .label{font-weight: bold; color: #555;}"
                + ".rating{font-size: 20px;}"
                + ".comment-box{background-color: #f9f9f9; border-left: 4px solid #007bff; padding: 15px; margin-top: 15px; font-style: italic;}"
                + ".footer{margin-top: 20px; font-size: 12px; color: #888; text-align: center;}"
                + ".button{display: inline-block; padding: 10px 20px; background-color: #007bff; color: #ffffff; text-decoration: none; border-radius: 5px; margin-top: 15px;}"
                + "</style></head><body>"
                + "<div class='container'>"
                + "<div class='header'>" + reviewType + "</div>"
                + "<div class='content'>"
                + "<p>Assalomu alaykum, <strong>" + getFullName(instructor) + "</strong>,</p>"
                + "<p>Sizning \"<strong>" + course.getTitle() + "</strong>\" nomli kursingizga yangi sharh qoldirildi.</p>"
                + "<hr>"
                + "<p><span class='label'>Sharh muallifi:</span> " + getFullName(reviewer) + "</p>"
                + "<p><span class='label'>Baho:</span> <span class='rating'>" + generateStarRating(review.getRating()) + " (" + review.getRating() + "/5)</span></p>"
                + "<p class='label'>Izoh:</p>"
                + "<div class='comment-box'>" + comment + "</div>"
                + "<a href='" + courseLink + "' class='button' style='color: #ffffff;'>Kursni ko'rish</a>"
                + "</div>"
                + "<div class='footer'>"
                + "<p>&copy; " + java.time.Year.now().getValue() + " Online Education Platform. Barcha huquqlar himoyalangan.</p>"
                + "</div>"
                + "</div></body></html>";
    }
}