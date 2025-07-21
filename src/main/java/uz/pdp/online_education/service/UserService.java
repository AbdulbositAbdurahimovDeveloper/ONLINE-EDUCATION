package uz.pdp.online_education.service;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.payload.ResponseDTO;
import uz.pdp.online_education.payload.user.LoginDTO;
import uz.pdp.online_education.payload.user.UserDTO;
import uz.pdp.online_education.payload.user.UserRegisterRequestDTO;
import uz.pdp.online_education.payload.user.UserRegisterResponseDTO;

public interface UserService {
    User loadUserByUsername(String username);

    ResponseDTO<?> login(LoginDTO loginDTO);

    UserDTO register(UserRegisterRequestDTO request);

    void verifyAccount(String token);

    Page<User> read(Integer page, Integer size);

    UserDTO read(Long id);

    UserDTO update(Long id, UserRegisterRequestDTO registerRequestDTO);

    void delete(Long id);
}
