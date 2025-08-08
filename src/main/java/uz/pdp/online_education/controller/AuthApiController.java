package uz.pdp.online_education.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import uz.pdp.online_education.enums.Role;
import uz.pdp.online_education.exceptions.DataConflictException;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.model.UserProfile;
import uz.pdp.online_education.payload.ResponseDTO;
import uz.pdp.online_education.payload.errors.ErrorDTO;
import uz.pdp.online_education.payload.user.UserRegisterRequestDTO;
import uz.pdp.online_education.repository.TelegramUserRepository;
import uz.pdp.online_education.repository.UserProfileRepository;
import uz.pdp.online_education.repository.UserRepository;
import uz.pdp.online_education.service.AuthService;
import uz.pdp.online_education.service.interfaces.UserService;
import uz.pdp.online_education.telegram.enums.UserState;
import uz.pdp.online_education.telegram.model.TelegramUser;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {


    private final AuthService authService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final TelegramUserRepository telegramUserRepository;

    public AuthApiController(AuthService authService, UserService userService, PasswordEncoder passwordEncoder, TelegramUserRepository telegramUserRepository) {
        this.authService = authService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.telegramUserRepository = telegramUserRepository;
    }

    @PostMapping("/login-telegram")
    public ResponseEntity<ResponseDTO<String>> handleLogin(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("chat_id") Long chatId) {

        try {
            // Service'ga login va bog'lash uchun murojaat
            // authService.loginAndLinkTelegramAccount(username, password, chatId);

            User user = userService.loadUserByUsername(username);

            if (!user.isEnabled()){
                throw new DataConflictException("User is not enabled");
            }

            boolean matches = passwordEncoder.matches(password, user.getPassword());
            if (!matches) {
                throw new DataConflictException("Username or password invalid");
            }
            TelegramUser telegramUser = telegramUserRepository.findByChatId(chatId)
                    .orElseThrow(() -> new EntityNotFoundException("Telegram user not found with chatId:" + chatId));

            if (telegramUser.getUser() != null) {
                throw new DataConflictException("Telegram user already connected with id:" + chatId);
            }

            telegramUser.setUser(user);
            telegramUserRepository.save(telegramUser);

            String successMessage = "Muvaffaqiyatli ulandi! Endi botga qaytishingiz mumkin. hurmatli :" + user.getProfile().getFirstName() + " " + user.getProfile().getLastName();
            return ResponseEntity.ok(ResponseDTO.success(successMessage));

        } catch (Exception e) {
            // Masalan, AuthenticationException yoki EntityNotFoundException
            ErrorDTO errorDto = new ErrorDTO(HttpStatus.BAD_REQUEST.value(), e.getMessage());
            return ResponseEntity.badRequest().body(ResponseDTO.error(errorDto));
        }
    }

    @PostMapping("/register-telegram")
    public ResponseEntity<ResponseDTO<String>> handleRegister(
            @Valid @ModelAttribute UserRegisterRequestDTO userDto,
            BindingResult bindingResult,
            @RequestParam("chat_id") Long chatId) {

        if (bindingResult.hasErrors()) {
            String errorMessages = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.joining(", "));

            ErrorDTO errorDto = new ErrorDTO(HttpStatus.BAD_REQUEST.value(), errorMessages);
            return ResponseEntity.badRequest().body(ResponseDTO.error(errorDto));
        }

        try {
            authService.registerAndLinkTelegramAccount(userDto, chatId);

            String successMessage = "Muvaffaqiyatli ro‘yxatdan o‘tdingiz va profilingiz bog‘landi! Hurmatli " + userDto.getFirstName();
            return ResponseEntity.ok(ResponseDTO.success(successMessage));

        } catch (DataConflictException e) {
            ErrorDTO errorDto = new ErrorDTO(HttpStatus.CONFLICT.value(), e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ResponseDTO.error(errorDto));

        } catch (Exception e) {
            ErrorDTO errorDto = new ErrorDTO(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Ichki server xatoligi yuz berdi.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseDTO.error(errorDto));
        }
    }
}