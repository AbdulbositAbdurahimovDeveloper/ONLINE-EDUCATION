package uz.pdp.online_education.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.payload.ResponseDTO;
import uz.pdp.online_education.payload.user.*;

public interface UserService {
    User loadUserByUsername(String username);

    ResponseDTO<?> login(LoginDTO loginDTO);

    RegistrationResponseDTO register(UserRegisterRequestDTO request, HttpServletRequest httpServletRequest);

    void verifyAccount(String token);

    Page<User> read(Integer page, Integer size);

    UserDTO read(Long id);

    UserDTO update(Long id, UserRegisterRequestDTO registerRequestDTO);

    void delete(Long id);
}
