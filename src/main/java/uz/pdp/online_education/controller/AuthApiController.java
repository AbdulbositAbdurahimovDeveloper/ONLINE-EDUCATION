package uz.pdp.online_education.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import uz.pdp.online_education.exceptions.DataConflictException;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.payload.ResponseDTO;
import uz.pdp.online_education.payload.errors.ErrorDTO;
import uz.pdp.online_education.payload.user.UserRegisterRequestDTO;
import uz.pdp.online_education.repository.TelegramUserRepository;
import uz.pdp.online_education.service.AuthService;
import uz.pdp.online_education.service.interfaces.UserService;
import uz.pdp.online_education.telegram.model.TelegramUser;

import java.util.stream.Collectors;

/**
 * REST API controller responsible for authentication and Telegram account linking.
 * <p>
 * This controller exposes endpoints for:
 * <ul>
 *     <li>Login with username and password, then link with Telegram account</li>
 *     <li>Register new user and link account with Telegram</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final AuthService authService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final TelegramUserRepository telegramUserRepository;

    public AuthApiController(AuthService authService, UserService userService,
                             PasswordEncoder passwordEncoder, TelegramUserRepository telegramUserRepository) {
        this.authService = authService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.telegramUserRepository = telegramUserRepository;
    }

    /**
     * Login with username and password, then link with Telegram account.
     *
     * @param username the username of the user trying to log in
     * @param password the raw password provided by the user
     * @param chatId   the Telegram chat ID of the user
     * @return {@link ResponseEntity} containing a success message or an error
     */
    @Operation(
            summary = "Login and link Telegram account",
            description = "Logs in a user using username and password, then links the account with a Telegram chat ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully logged in and linked with Telegram"),
            @ApiResponse(responseCode = "400", description = "Invalid username or password / Bad request",
                    content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "404", description = "Telegram user not found with the given chat ID"),
            @ApiResponse(responseCode = "409", description = "Telegram user already linked with another account")
    })
    @PostMapping("/login-telegram")
    public ResponseEntity<ResponseDTO<String>> handleLogin(
            @Parameter(description = "Username of the user", example = "john_doe")
            @RequestParam("username") String username,

            @Parameter(description = "Password of the user", example = "secret123")
            @RequestParam("password") String password,

            @Parameter(description = "Telegram chat ID", example = "123456789")
            @RequestParam("chat_id") Long chatId) {

        try {
            User user = userService.loadUserByUsername(username);

            if (!user.isEnabled()) {
                throw new DataConflictException("User is not enabled");
            }

            boolean matches = passwordEncoder.matches(password, user.getPassword());
            if (!matches) {
                throw new DataConflictException("Username or password invalid");
            }

            TelegramUser telegramUser = telegramUserRepository.findByChatId(chatId)
                    .orElseThrow(() -> new EntityNotFoundException("Telegram user not found with chatId: " + chatId));

            if (telegramUser.getUser() != null) {
                throw new DataConflictException("Telegram user already connected with id: " + chatId);
            }

            telegramUser.setUser(user);
            telegramUserRepository.save(telegramUser);

            String successMessage = "Successfully linked! Welcome, " +
                    user.getProfile().getFirstName() + " " + user.getProfile().getLastName();
            return ResponseEntity.ok(ResponseDTO.success(successMessage));

        } catch (Exception e) {
            ErrorDTO errorDto = new ErrorDTO(HttpStatus.BAD_REQUEST.value(), e.getMessage());
            return ResponseEntity.badRequest().body(ResponseDTO.error(errorDto));
        }
    }

    /**
     * Register a new user via Telegram and link the account.
     *
     * @param userDto DTO containing registration data such as username, email, password
     * @param bindingResult validation results for the provided DTO
     * @param chatId Telegram chat ID for linking
     * @return success or error response
     */
    @Operation(
            summary = "Register and link Telegram account",
            description = "Registers a new user with provided data and links the account with a Telegram chat ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully registered and linked with Telegram"),
            @ApiResponse(responseCode = "400", description = "Validation failed / Bad request"),
            @ApiResponse(responseCode = "409", description = "Conflict: Username or email already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/register-telegram")
    public ResponseEntity<ResponseDTO<String>> handleRegister(
            @Valid @ModelAttribute UserRegisterRequestDTO userDto,
            BindingResult bindingResult,

            @Parameter(description = "Telegram chat ID", example = "123456789")
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

            String successMessage = "Successfully registered and linked! Welcome, " + userDto.getFirstName();
            return ResponseEntity.ok(ResponseDTO.success(successMessage));

        } catch (DataConflictException e) {
            ErrorDTO errorDto = new ErrorDTO(HttpStatus.CONFLICT.value(), e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ResponseDTO.error(errorDto));

        } catch (Exception e) {
            ErrorDTO errorDto = new ErrorDTO(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "An internal server error occurred.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseDTO.error(errorDto));
        }
    }

}
