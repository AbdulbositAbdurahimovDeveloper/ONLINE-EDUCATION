package uz.pdp.online_education.service.interfaces;

public interface EmailService {
    void sendVerificationEmail(String email, String confirmationUrl);
}
