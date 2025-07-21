package uz.pdp.online_education.service;

public interface ValidationService {
    boolean isUsernameAvailable(String username);

    boolean isEmailAvailable(String email);
}
