package uz.pdp.online_education.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import uz.pdp.online_education.exceptions.DataConflictException;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.payload.errors.ErrorDTO;
import uz.pdp.online_education.payload.telegramm_bot.ApiResponseDTO;
import uz.pdp.online_education.payload.telegramm_bot.LoginDTO;
import uz.pdp.online_education.payload.telegramm_bot.RegisterDTO;
import uz.pdp.online_education.payload.telegramm_bot.TelegramUserData;
import uz.pdp.online_education.payload.user.UserRegisterRequestDTO;
import uz.pdp.online_education.repository.TelegramUserRepository;
import uz.pdp.online_education.service.AuthService;
import uz.pdp.online_education.service.interfaces.UserService;
import uz.pdp.online_education.telegram.model.TelegramUser;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api/admin/telegram")
@RequiredArgsConstructor
public class TelegramWebhookController {

    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final TelegramUserRepository telegramUserRepository;
    private final AuthService authService;

    @Value("${telegram.bot.token}")
    private String botToken;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/set-webhook")
    public ResponseEntity<String> setWebhook(@RequestParam String domain) {
        String webhookUrl = domain.replaceAll("/+$", "") + "/telegram-bot";
        String url = "https://api.telegram.org/bot" + botToken + "/setWebhook?url=" + webhookUrl;

        String response = restTemplate.getForObject(url, String.class);
        return ResponseEntity.ok(response);
    }

    // --- Mini App uchun LOGIN endpointi ---
    @PostMapping("/login")
    public ResponseEntity<ApiResponseDTO> handleLogin(@RequestBody LoginDTO loginDTO) {

        if (!isDataValid(loginDTO.getInitData())) {
            return ResponseEntity.status(403).body(new ApiResponseDTO(false, "Xavfsizlik tekshiruvidan o'tmadi!"));
        }

        try {
            TelegramUserData telegramUserData = parseTelegramUser(loginDTO.getInitData());
            User user = userService.loadUserByUsername(loginDTO.getUsername());

            if (!user.isEnabled()) {
                throw new DataConflictException("User is not enabled");
            }

            boolean matches = passwordEncoder.matches(loginDTO.getPassword(), user.getPassword());
            if (!matches) {
                throw new DataConflictException("Username or password invalid");
            }

            TelegramUser telegramUser = telegramUserRepository.findByChatId(telegramUserData.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Telegram user not found with chatId: " + telegramUserData.getId()));

            if (telegramUser.getUser() != null) {
                throw new DataConflictException("Telegram user already connected with id: " + telegramUserData.getId());
            }

            telegramUser.setUser(user);
            telegramUserRepository.save(telegramUser);

            String successMessage = "Successfully linked! Welcome, " +
                    user.getProfile().getFirstName() + " " + user.getProfile().getLastName();
            return ResponseEntity.ok(new ApiResponseDTO(true, successMessage));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ApiResponseDTO(false, "Serverda ichki xatolik: " + e.getMessage()));
        }
    }

    // --- Mini App uchun REGISTER endpointi ---
    @PostMapping("/register")
    public ResponseEntity<ApiResponseDTO> handleRegister(@RequestBody RegisterDTO registerDTO) {

        if (!isDataValid(registerDTO.getInitData())) {
            return ResponseEntity.status(403).body(new ApiResponseDTO(false, "Xavfsizlik tekshiruvidan o'tmadi!"));
        }

        try {
            TelegramUserData telegramUser = parseTelegramUser(registerDTO.getInitData());


            UserRegisterRequestDTO userDto = new UserRegisterRequestDTO(
                    registerDTO.getUsername(),
                    registerDTO.getPassword(),
                    telegramUser.getFirstName(),
                    telegramUser.getLastName(),
                    registerDTO.getUsername(),
                    null,
                    null,
                    registerDTO.getPhone()
            );
            authService.registerAndLinkTelegramAccount(userDto, telegramUser.getId());

            return ResponseEntity.ok(new ApiResponseDTO(true, "Muvaffaqiyatli ro'yxatdan o'tdingiz!"));

        } catch (DataConflictException e) {
            ErrorDTO errorDto = new ErrorDTO(HttpStatus.CONFLICT.value(), e.getMessage());
//            return ResponseEntity.status(HttpStatus.CONFLICT).body(ResponseDTO.error(errorDto));
            return ResponseEntity.status(409).body(new ApiResponseDTO(false, errorDto.getMessage()));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ApiResponseDTO(false, "Serverda ichki xatolik: " + e.getMessage()));
        }
    }


    private boolean isDataValid(String initData) {
        try {
            Map<String, String> params = new HashMap<>();
            for (String param : initData.split("&")) {
                String[] pair = param.split("=", 2);
                params.put(pair[0], URLDecoder.decode(pair[1], StandardCharsets.UTF_8));
            }

            String hash = params.remove("hash");
            if (hash == null) {
                return false;
            }

            List<String> keys = new ArrayList<>(params.keySet());
            Collections.sort(keys);

            StringBuilder dataCheckString = new StringBuilder();
            for (int i = 0; i < keys.size(); i++) {
                String key = keys.get(i);
                dataCheckString.append(key).append("=").append(params.get(key));
                if (i < keys.size() - 1) {
                    dataCheckString.append("\n");
                }
            }

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec1 = new SecretKeySpec("WebAppData".getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec1);
            byte[] secretKey = mac.doFinal(botToken.getBytes(StandardCharsets.UTF_8));

            SecretKeySpec secretKeySpec2 = new SecretKeySpec(secretKey, "HmacSHA256");
            mac.init(secretKeySpec2);
            byte[] hashBytes = mac.doFinal(dataCheckString.toString().getBytes(StandardCharsets.UTF_8));

            String calculatedHash = DatatypeConverter.printHexBinary(hashBytes).toLowerCase();
            return calculatedHash.equals(hash);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private TelegramUserData parseTelegramUser(String initData) throws Exception {
        for (String param : initData.split("&")) {
            String[] pair = param.split("=", 2);
            if (pair[0].equals("user")) {
                String decodedUser = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                return objectMapper.readValue(decodedUser, TelegramUserData.class);
            }
        }
        throw new Exception("initData ichida 'user' parametri topilmadi");
    }
}