package uz.pdp.online_education.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.online_education.payload.ResponseDTO;
import uz.pdp.online_education.payload.user.*;
import uz.pdp.online_education.service.interfaces.UserService;

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
    public ResponseEntity<ResponseDTO<?>> register(@RequestBody @Valid UserRegisterRequestDTO responseDTO,
                                                   HttpServletRequest httpServletRequest) {
        RegistrationResponseDTO register = userService.register(responseDTO,httpServletRequest);
        return ResponseEntity.ok(ResponseDTO.success(register));
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyAccount(@RequestParam("token") String token) {
        userService.verifyAccount(token);
        return ResponseEntity.ok("Hisob muvaffaqiyatli tasdiqlandi! Endi tizimga kirishingiz mumkin.");
    }

}
