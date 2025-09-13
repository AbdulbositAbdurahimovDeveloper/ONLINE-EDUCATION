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
    private final TemplateEngine templateEngine;

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

            Context context = new Context();
            context.setVariable("username", email);
            context.setVariable("confirmationUrl", confirmationUrl);


            String htmlContent = templateEngine.process("verification-email", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("Online Education - Hisobni Tasdiqlash");
            helper.setText(htmlContent, true);
            helper.setFrom("noreply@online-education.com");



        } catch (MessagingException e) {

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

            Context context = new Context();


            BigDecimal amountInSom = BigDecimal.valueOf(payment.getAmount()).divide(new BigDecimal(100));
            String formattedAmount = NumberFormat.getCurrencyInstance(new Locale("uz", "UZ")).format(amountInSom);
            String formattedDate = payment.getCreatedAt().toLocalDateTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm"));
            String maskedCard = "**** **** **** " + payment.getMaskedCardNumber().substring(12);


            context.setVariable("recipientName", user.getProfile().getFirstName());
            context.setVariable("moduleTitle", payment.getModule().getTitle());
            context.setVariable("paymentId", payment.getId());
            context.setVariable("paymentDate", formattedDate);
            context.setVariable("maskedCard", maskedCard);
            context.setVariable("formattedAmount", formattedAmount);
            context.setVariable("currentYear", java.time.Year.now().getValue());

            String htmlContent = templateEngine.process("payment-receipt", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(user.getProfile().getEmail());
            helper.setSubject("To'lov cheki: " + payment.getModule().getTitle());
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);


            log.info("Successfully sent payment receipt email to {}", user.getProfile().getEmail());

        } catch (MessagingException e) {
            log.error("Failed to send payment receipt email to {}: {}", user.getProfile().getEmail(), e.getMessage());

        }
    }


    @Async
    @Override
    public void sendSimpleNotification(String to, String subject, String text) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, true);
            helper.setFrom(fromEmail);

            mailSender.send(mimeMessage);
            log.info("Successfully sent notification email to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send notification email to {}: {}", to, e.getMessage());
        }
    }
}

