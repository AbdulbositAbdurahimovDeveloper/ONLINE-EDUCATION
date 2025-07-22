package uz.pdp.online_education.service;

public interface EmailService {
    void sendVerificationEmail(String email, String confirmationUrl);
}
