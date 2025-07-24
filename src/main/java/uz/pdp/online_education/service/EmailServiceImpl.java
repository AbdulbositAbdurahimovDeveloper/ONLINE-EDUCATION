package uz.pdp.online_education.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine; // Thymeleaf'dan HTML generatsiya qilish uchun

    /**
     * @param email
     * @param confirmationUrl
     */

    @Async
    @Override
    public void sendVerificationEmail(String email, String confirmationUrl) {

        try {
            // 1. Thymeleaf kontekstini yaratish va shablonga o'zgaruvchilarni uzatish
            Context context = new Context();
            context.setVariable("username", email); // Yoki user.getProfile().getFirstName()
            context.setVariable("confirmationUrl", confirmationUrl);

            // 2. HTML shablonini String'ga aylantirish
            String htmlContent = templateEngine.process("verification-email", context);

            // 3. MimeMessage (HTML'li xabar) yaratish
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("Online Education - Hisobni Tasdiqlash");
            helper.setText(htmlContent, true); // 'true' - bu HTML ekanligini bildiradi
            helper.setFrom("noreply@online-education.com"); // Bu email mavjud bo'lishi shart emas

            // 4. Xabarni jo'natish
            mailSender.send(mimeMessage);

        } catch (MessagingException e) {
            // Bu yerda xatolikni log qilish kerak.
            // Masalan: log.error("Failed to send verification email to {}", user.getEmail(), e);
            // Production'da bu xatoliklarni kuzatib borish muhim.
            throw new IllegalStateException("Failed to send email");
        }
    }


}

