package uz.pdp.online_education.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.online_education.payload.ResponseDTO;
import uz.pdp.online_education.payload.user.LoginDTO;
import uz.pdp.online_education.payload.user.UserDTO;
import uz.pdp.online_education.payload.user.UserRegisterRequestDTO;
import uz.pdp.online_education.payload.user.UserRegisterResponseDTO;
import uz.pdp.online_education.service.UserService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<ResponseDTO<?>> login(@RequestBody @Valid LoginDTO loginDTO) {
        ResponseDTO<?> login = userService.login(loginDTO);
        return ResponseEntity.ok(login);
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseDTO<UserDTO>> register(@RequestBody @Valid UserRegisterRequestDTO responseDTO) {
        UserDTO userDTO = userService.register(responseDTO);
        return ResponseEntity.ok(ResponseDTO.success(userDTO));
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyAccount(@RequestParam("token") String token) {
        userService.verifyAccount(token);
        return ResponseEntity.ok("Hisob muvaffaqiyatli tasdiqlandi! Endi tizimga kirishingiz mumkin.");
    }

}
