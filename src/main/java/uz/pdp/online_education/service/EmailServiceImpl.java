package uz.pdp.online_education.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import uz.pdp.online_education.model.Payment;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.service.interfaces.EmailService;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;


@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine; // Thymeleaf'dan HTML generatsiya qilish uchun

    @Value("${spring.mail.username}")
    private String fromEmail;

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

    /**
     * Generates and sends a payment receipt using a Thymeleaf template.
     * @param payment The successfully completed payment entity.
     */
    @Async
    @Override
    public void sendPaymentReceipt(Payment payment) {
        User user = payment.getUser();
        if (user == null || user.getProfile() == null || user.getProfile().getEmail() == null) {
            log.warn("Cannot send receipt for payment ID {}: User or email info is missing.", payment.getId());
            return;
        }

        try {
            // 1. Thymeleaf kontekstini yaratish va shablonga o'zgaruvchilarni uzatish
            Context context = new Context();

            // Ma'lumotlarni formatlash
            BigDecimal amountInSom = BigDecimal.valueOf(payment.getAmount()).divide(new BigDecimal(100));
            String formattedAmount = NumberFormat.getCurrencyInstance(new Locale("uz", "UZ")).format(amountInSom);
            String formattedDate = payment.getCreatedAt().toLocalDateTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm"));
            String maskedCard = "**** **** **** " + payment.getMaskedCardNumber().substring(12);

            // O'zgaruvchilarni kontekstga o'rnatish
            context.setVariable("recipientName", user.getProfile().getFirstName());
            context.setVariable("moduleTitle", payment.getModule().getTitle());
            context.setVariable("paymentId", payment.getId());
            context.setVariable("paymentDate", formattedDate);
            context.setVariable("maskedCard", maskedCard);
            context.setVariable("formattedAmount", formattedAmount);
            context.setVariable("currentYear", java.time.Year.now().getValue());

            // 2. HTML shablonini String'ga aylantirish
            String htmlContent = templateEngine.process("payment-receipt", context);

            // 3. MimeMessage (HTML'li xabar) yaratish
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(user.getProfile().getEmail());
            helper.setSubject("To'lov cheki: " + payment.getModule().getTitle());
            helper.setText(htmlContent, true); // 'true' - bu HTML ekanligini bildiradi
            helper.setFrom(fromEmail);

            // 4. Xabarni jo'natish
//            mailSender.send(mimeMessage);
            log.info("Successfully sent payment receipt email to {}", user.getProfile().getEmail());

        } catch (MessagingException e) {
            log.error("Failed to send payment receipt email to {}: {}", user.getProfile().getEmail(), e.getMessage());
            // Production'da bu xatoliklarni kuzatib borish muhim.
            // Bu yerda IllegalStateException otish shart emas, chunki bu fon rejimida ishlayapti
            // va asosiy operatsiyaga ta'sir qilmasligi kerak.
        }
    }
    // EmailServiceImpl klassining ichiga shu metodni qo'shing

    @Async
    @Override
    public void sendSimpleNotification(String to, String subject, String text) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, false); // 'false' - bu oddiy matn ekanligini bildiradi
            helper.setFrom(fromEmail);

            mailSender.send(mimeMessage);
            log.info("Successfully sent notification email to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send notification email to {}: {}", to, e.getMessage());
        }
    }

    @Async
//    @Override
    public void sendReviewNotification(String to, String subject, String body) {
        try {
            // 1. Thymeleaf kontekstini yaratamiz
            Context context = new Context();
            context.setVariable("subject", subject);
            // Matndagi qator tashlashlarni (\n) HTML'dagi <br> ga o'giramiz
            context.setVariable("body", body.replace("\n", "<br />"));
            context.setVariable("platformUrl", "http://online-education.com"); // Platforma URL'si
            context.setVariable("currentYear", java.time.Year.now().getValue());

            // 2. HTML shablonni String'ga aylantiramiz
            String htmlContent = templateEngine.process("review-notification", context);

            // 3. MimeMessage (HTML'li xabar) yaratish
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // 'true' - bu HTML ekanligini bildiradi
            helper.setFrom(fromEmail);

            // 4. Xabarni jo'natish
            mailSender.send(mimeMessage);
            log.info("Successfully sent review notification email to {}", to);

        } catch (MessagingException e) {
            log.error("Failed to send review notification email to {}: {}", to, e.getMessage());
        }
    }
}

