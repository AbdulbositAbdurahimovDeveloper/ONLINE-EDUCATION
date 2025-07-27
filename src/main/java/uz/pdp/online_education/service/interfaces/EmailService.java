package uz.pdp.online_education.service.interfaces;

import org.springframework.scheduling.annotation.Async;
import uz.pdp.online_education.model.Payment;

public interface EmailService {
    void sendVerificationEmail(String email, String confirmationUrl);


    void sendPaymentReceipt(Payment payment);
}
