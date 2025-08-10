package uz.pdp.online_education.service;

import org.springframework.transaction.annotation.Transactional;
import uz.pdp.online_education.payload.user.UserRegisterRequestDTO;

public interface AuthService {

    void registerAndLinkTelegramAccount(UserRegisterRequestDTO request, Long chatId);
}
