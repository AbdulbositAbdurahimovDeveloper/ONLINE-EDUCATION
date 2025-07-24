package uz.pdp.online_education.service.interfaces;

public interface ValidationService {
    boolean isUsernameAvailable(String username);

    boolean isEmailAvailable(String email);
}
