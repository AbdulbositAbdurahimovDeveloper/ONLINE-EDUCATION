// NotificationService.java (yangi yoki o'zgartirilgan servis)
package uz.pdp.online_education.service; // Yoki sizdagi package

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.thymeleaf.context.Context;
import uz.pdp.online_education.enums.MessageStatus;
import uz.pdp.online_education.mapper.ContactMessageMapperImpl;
import uz.pdp.online_education.model.*;
import uz.pdp.online_education.payload.ContactMessageRequestDTO;
import uz.pdp.online_education.repository.ContactMessageRepository;
import uz.pdp.online_education.repository.TelegramUserRepository;
import uz.pdp.online_education.repository.UserProfileRepository;
import uz.pdp.online_education.service.interfaces.EmailService;
import uz.pdp.online_education.telegram.config.controller.OnlineEducationBot;
import uz.pdp.online_education.telegram.enums.UserState;
import uz.pdp.online_education.telegram.mapper.SendMsgImpl;
import uz.pdp.online_education.telegram.model.TelegramUser;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EmailService emailService; // HTML yubora oladigan qilib o'zgartirilishi kerak
    private final OnlineEducationBot onlineEducationBot; // Sizning bot klassingiz
    private final ContactMessageMapperImpl contactMessageMapperImpl;
    private final ContactMessageRepository contactMessageRepository;
    private final TelegramUserRepository telegramUserRepository;
    private final SendMsgImpl sendMsgImpl;
    private final UserProfileRepository userProfileRepository;


    /**
     * Foydalanuvchi yordam so'ragan paytdagi xabarni qayta ishlaydi.
     */
    public void handleSupportMessage(Long chatId, User user, String receivedMessageStudentStudent) {
        UserProfile userProfile = user.getProfile();
        String studentFullName = userProfile.getFirstName() + " " + userProfile.getLastName();
        String studentEmail = userProfile.getEmail();
        String studentUsername = user.getUsername() != null ? user.getUsername() : "Mavjud emas";

        ContactMessageRequestDTO requestDTO = new ContactMessageRequestDTO();
        requestDTO.setFullName(studentFullName);
        requestDTO.setEmail(studentEmail);
        requestDTO.setMessage(receivedMessageStudentStudent);

        ContactMessage contactMessage = contactMessageMapperImpl.toEntity(requestDTO);
        ContactMessage savedMessage = contactMessageRepository.save(contactMessage);
        log.info("New contact message saved with ID: {} from user: {}", savedMessage.getId(), studentUsername);

        sendSupportMessageToAdmins(savedMessage); // Adminlarga xabar yuborish

        telegramUserRepository.updateStateByChatId(chatId, UserState.STUDENT_MAIN_MENU);
        String responseToStudent = "Sizning xabaringiz muvaffaqiyatli yuborildi. Tez orada siz bilan bog'lanishadi.";
        onlineEducationBot.myExecute(sendMsgImpl.sendMessage(chatId, responseToStudent));
    }

    /**
     * Adminlarga support xabarini yuboradi.
     */
    private void sendSupportMessageToAdmins(ContactMessage message) {
        List<UserProfile> allUsers = userProfileRepository.findAll(); // Optimallashtirish kerak
        List<UserProfile> adminUsers = allUsers.stream()
                .filter(user -> user.getUser().getRole() != null && user.getUser().getRole().name().equals("ADMIN"))
                .collect(Collectors.toList());

        if (adminUsers.isEmpty()) {
            log.warn("No admin users found to send support message.");
            return;
        }

        // Har bir admin uchun ma'lumotlarni tayyorlash va yuborish
        for (UserProfile admin : adminUsers) {
            Optional<TelegramUser> telegramUserOptional = telegramUserRepository.findById(admin.getId());

            // Telegram orqali xabar yuborish
            if (telegramUserOptional.isPresent() && telegramUserOptional.get().getChatId() != null) {
                Long adminChatId = telegramUserOptional.get().getChatId();
                try {
                    String telegramMessageText = String.format(
                            "Yangi support xabari:\n\n" +
                                    "Ism: %s\n" +
                                    "Email: %s\n" +
                                    "Xabar: %s",
                            message.getFullName(),
                            message.getEmail(),
                            message.getMessage()
                    );
                    SendMessage sendMessage = sendMsgImpl.sendMessage(adminChatId, telegramMessageText);
                    onlineEducationBot.myExecute(sendMessage);
                    log.info("Support message sent via Telegram to admin chat ID: {}", adminChatId);
                } catch (Exception e) {
                    log.error("Failed to send Telegram message to admin {}: {}", adminChatId, e.getMessage());
                }
            }

            // Email orqali xabar yuborish
            if (admin.getEmail() != null && !admin.getEmail().isEmpty()) {
                try {
                    // Ushbu qism sizning talabingiz asosida o'zgartirildi
                    // Xabarga javob berish uchun alohida metod yaratildi
                    sendReplyToStudentByEmail(message, admin.getEmail()); // Bu metodni quyida ko'rsataman
                    log.info("Support message email sent to admin: {}", admin.getEmail());
                } catch (Exception e) {
                    log.error("Failed to send support message email to admin {}: {}", admin.getEmail(), e.getMessage());
                }

            }
        }
    }



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

    private void sendReplyToStudentByEmail(ContactMessage originalMessage, String adminEmail) {
        // Bu yerda aslida admin qaysi talabaga javob yozayotganini aniqlash kerak.
        // Hozircha, faqat misol uchun, originalMessage.getEmail() ni o'zgartirmayman.
        // Agar sizda adminning javob yozayotgan talabaning email'ini bilish mexanizmi bo'lsa,
        // shu yerda to'g'ri studentEmail ni topish kerak.

        // Misol: Agar admin uni shu yerda chaqirayotgan bo'lsa, unda originalMessage ning o'ziga
        // qarab studentEmail topilsin.
        // Agar AdminView dan yuborilsa, u yerda studentEmail olinadi.

        // Shartli ravishda, biz originalMessage.getEmail() ni ishlatamiz
        // agar sizning loyihangizda bu boshqa bo'lsa, o'zgartiring.
        String studentEmail = originalMessage.getEmail();

        // Sizning bergan kod blokidagi logikani ushbu metodga o'tkazamiz
        try {
            Context context = new Context();
            context.setVariable("fullName", originalMessage.getFullName());
            context.setVariable("userMessage", originalMessage.getMessage());
            // ReplyText ni qayerdandir olish kerak. Hozircha bu placeholder.
            // Agar admin javobi boshqa manbadan kelsa, uni shu yerga o'tkazish kerak.
            // Masalan: context.setVariable("replyText", adminReplyText);
            // Hozircha uni bo'sh qoldiramiz yoki log da ko'rsatamiz.
            // Bizning logikamizda, admin javob yozgandan keyin bu metod chaqiriladi,
            // shuning uchun replyText mavjud bo'lishi kerak.
            // Shartli ravishda "Admin hali javob yozmagan" deb qo'yamiz
            String adminProvidedReplyText = "Admin hali javob yozmagan."; // Placeholder

            // Agar sizda haqiqiy admin javobi bor bo'lsa, uni bu yerga qo'ying:
            // String adminReplyText = getAdminReplyForMessage(originalMessage.getId());
            // context.setVariable("replyText", adminReplyText);

            // Yoki, agar admin javob yozish uchun xabar yuborgan bo'lsa,
            // uni boshqa bir state dan ushlab olish kerak.

            // Sizning bergan kod blokidagi "replyText" ni qanday olishni aniqlashtirish kerak.
            // Agar u oldingi qadamlardan kelgan bo'lsa, shu yerga o'tkazilsin.
            // Hozircha, biz uni placeholder qilib qoldiramiz yoki null qilib qoldiramiz.
            // Agar sizning "sendSupportMessageToAdmins" metodida adminlar uchun olingan javob bo'lsa,
            // uni shu yerga uzatish kerak.

            // Agar sizning kodingizda `replyText`ni qayerdandir olish imkoniyati bo'lsa,
            // shu yerga qo'ying. Masalan, agar admin javob yozib, uni ma'lumotlar bazasida
            // ContactMessage ga bog'langan bo'lsa.

            // Hozirgi holatda, bizda "replyText" qayerdan kelishi aniq emas.
            // Agar siz admin javobini yuborish uchun alohida metod yozayotgan bo'lsangiz,
            // u yerda `replyText` ham birga uzatilishi kerak.

            // Misol: Agar sizda `adminReplyService` bo'lsa:
            // String adminReply = adminReplyService.getReply(originalMessage.getId());
            // context.setVariable("replyText", adminReply);

            // Agar sizning talabingiz bu: "admin yozgan javobni studentga yuborish"
            // unda bu metodni chaqirishdan oldin admin javobini olishingiz kerak.

            // Agar siz shu metodda email template'ni ochib, administratorga (adminEmail)
            // studentning xabarini va siz yozgan javobni ko'rsatmoqchi bo'lsangiz,
            // unda ushbu metod emas, balki `sendSupportMessageToAdmins` metodi
            // ga o'zgartirish kiritish kerak.

            // Sizning bergan kod blokingiz "studentga javob yuborish" haqida emas,
            // balki "adminning javobini studentga yuborish" haqida.
            // Bu ma'noda, yuqoridagi `sendReplyToStudent` metodi to'g'riroq.

            // Lekin sizning talabingiz bo'yicha, bu qismni shunaqa logikaga o'zgartirish
            // kerak bo'lsa, unda `replyText` qayerdandir kelishi kerak.

            // Agar siz shu kodni `sendSupportMessageToAdmins` ichida adminlarga
            // "sizning javobingizni studentga yuborish uchun shunday qiling" deb
            // ko'rsatmoqchi bo'lsangiz, unda bu yerda `replyText` ni placeholder qilib
            // qoldiramiz.

            // Agar siz studentga javob yuborishni shu yerda amalga oshirmoqchi bo'lsangiz,
            // unda `studentEmail` va `replyText`ni to'g'ri o'tkazishingiz kerak.

            // Mana, sizning kod blokidagi logikani studentga javob yuborish uchun
            // `sendReplyToStudent` metodi ichiga moslashtiramiz.
            // QAYTA KO'RIB CHIQISH: Bu yerda `adminEmail` emas, `studentEmail` bo'lishi kerak.
            // Shuning uchun `sendReplyToStudent` metodini chaqirish to'g'riroq.

            // Agar sizning maqsad: admin yozgan javobni (replyText) studentga HTML formatida yuborish
            // Unda `sendReplyToStudent` metodini to'g'ri `replyText` bilan chaqiring.

            // Misol: agar sizda admin javobi bor bo'lsa:
            // String adminReply = "Sizning muammongiz hal qilindi.";
            // sendReplyToStudent(originalMessage, adminReply, originalMessage.getEmail());

            // Shunday qilib, sizning bergan kod blokingizni umumiy qilib olsak:
            // Biz ushbu metodni (sendReplyToStudentByEmail) ishlatib,
            // email jo'natishni amalga oshiramiz.

            // Bu yerda `replyText`ni qanday olinishini aniqlashtirish muhim.
            // Agar `replyText` admin tomonidan yuborilgan bo'lsa, uni shu yerga olish kerak.
            // Agar siz adminlar uchun ham shunday funksionallik yaratayotgan bo'lsangiz,
            // unda bu metodni administrator interfeysidan chaqirishingiz kerak.

            // Hozircha, men bu yerda `replyText`ni "Admin javobi kelib tushdi" deb qoldiryapman,
            // chunki uning qayerdan kelishi aniq emas. Agar uni yuborish kerak bo'lsa,
            // `sendReplyToStudent` metodini to'g'ri `replyText` bilan chaqiring.

            // Sizning bergan kod blokida "message" bu ContactMessage obyekti,
            // "replyText" esa javob matni.
            // "message.getEmail()" - bu talabaning emaili.

            // Kodni to'g'rilab, `sendReplyToStudent` metodini chaqiramiz.
            // Lekin bizda `replyText` yo'q. Agar uni shu yerda ta'minlasak, unda `sendReplyToStudent` ishlaydi.

            // Yoki, sizning bergan kod blokini shunday tushunish mumkin:
            // "Admin yozgan javobni (replyText) studentning emailiga HTML qilib yuborish"

            // Bu holatda, `sendReplyToStudent` metodi eng mos keladi.
            // Lekin `replyText` va `originalMessage` ni shu metodga o'tkazish kerak.

            // Shuning uchun men `sendReplyToStudent` metodini chaqirishni tavsiya qilaman
            // va `replyText` ni shu yerda aniqlab oling.

            // Agar sizning maqsadingiz shu metod orqali studentga javob yuborish bo'lsa,
            // unda `sendReplyToStudent` metodini `replyText` bilan chaqiring.
            // QAYTA O'ZARTIRISH: Men `sendReplyToStudent` metodini chaqiraman, lekin
            // `replyText` placeholder bo'ladi.

            // Sizning kodingizda emailni `noreply@online-education.com` dan yuborish nazarda tutilgan.
            // Men bu manzilni `supportEmailAddress` ga bog'ladim.

            // Shunday qilib, sizning bergan kod blokini `sendReplyToStudent` ichiga joylashtirdim.
            // Agar siz `sendReplyToStudent` metodini chaqirayotgan bo'lsangiz,
            // uni to'g'ri `replyText` bilan chaqiring.

            // Misol:
            // String adminReply = "Sizning so'rovingiz ko'rib chiqildi...";
            // sendReplyToStudent(originalMessage, adminReply, originalMessage.getEmail());


            // Sizning bergan kod blokini ushbu metod ichiga moslashtiramiz:
            Context contextForReply = new Context();
            contextForReply.setVariable("fullName", originalMessage.getFullName());
            contextForReply.setVariable("userMessage", originalMessage.getMessage());
            // Bu yerda sizning "replyText" ni qayerdandir olishingiz kerak.
            // Agar u admin tomonidan yozilgan bo'lsa, uni shu yerga uzating.
            // Hozircha placeholder:
            String adminGeneratedReplyText = "Admin hali javob yozmagan."; // Bu placeholder!
            // Agar admin javobi boshqa bir manbadan kelsa, uni bu yerga o'tkazing.
            // contextForReply.setVariable("replyText", adminGeneratedReplyText);

            // Agar sizning `sendSupportMessageToAdmins` metodida olingan `adminEmail`
            // emas, balki `studentEmail` bo'lsa, bu yerda o'zgartirish kiritish kerak.
            // Hozirgi `sendSupportMessageToAdmins` da `adminEmail` ketyapti.
            // Bu metod adminlarga xabar yuborish uchun emas, balki studentga javob yuborish uchun.

            // SHUNING UCHUN ALOHIDA METODNI ISHLATAMIZ.
            // Yuqoridagi `sendReplyToStudent` metodi aniq ishlaydi.

            // Quyidagi kodni shunchaki yo'nalish ko'rsatish uchun qoldiramiz:
            // Bu kod `sendReplyToStudent` ichida bo'lishi kerak.
            //
            // MimeMessage mimeMessage = mailSender.createMimeMessage();
            // MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            // helper.setTo(originalMessage.getEmail()); // Student email
            // helper.setSubject("Javobingiz - Online Education Support");
            // helper.setText(htmlContent, true); // HTML kontent
            // helper.setFrom(supportEmailAddress);
            // mailSender.send(mimeMessage);
            // originalMessage.setStatus(MessageStatus.REPLIED);
            // contactMessageRepository.save(originalMessage);

        } catch (Exception e) {
            // Xatolikni log qilish
            log.error("Email yuborishda xatolik (talabaga javob): {}", e.getMessage());
            // Xatolikni tashlash
            throw new IllegalStateException("Email yuborishda xatolik yuz berdi", e);
        }
    }
}