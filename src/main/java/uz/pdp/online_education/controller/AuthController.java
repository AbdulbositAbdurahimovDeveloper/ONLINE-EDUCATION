    package uz.pdp.online_education.controller;

    import io.swagger.v3.oas.annotations.Operation;
    import io.swagger.v3.oas.annotations.Parameter;
    import io.swagger.v3.oas.annotations.media.Content;
    import io.swagger.v3.oas.annotations.media.Schema;
    import io.swagger.v3.oas.annotations.responses.ApiResponse;
    import io.swagger.v3.oas.annotations.responses.ApiResponses;
    import jakarta.servlet.http.HttpServletRequest;
    import jakarta.validation.Valid;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;
    import uz.pdp.online_education.payload.ResponseDTO;
    import uz.pdp.online_education.payload.user.LoginDTO;
    import uz.pdp.online_education.payload.user.RegistrationResponseDTO;
    import uz.pdp.online_education.payload.user.UserRegisterRequestDTO;
    import uz.pdp.online_education.service.interfaces.UserService;

    @RestController
    @RequestMapping("/api/auth")
    @RequiredArgsConstructor
    public class AuthController {

        private final UserService userService;

        @Operation(
                summary = "User login",
                description = "Authenticate a user with email/username and password, returning an access token."
        )
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Successfully logged in",
                        content = @Content(mediaType = "application/json",
                                schema = @Schema(implementation = ResponseDTO.class))),
                @ApiResponse(responseCode = "400", description = "Invalid login request"),
                @ApiResponse(responseCode = "401", description = "Unauthorized: Incorrect credentials")
        })
        @PostMapping("/login")
        public ResponseEntity<ResponseDTO<?>> login(
                @io.swagger.v3.oas.annotations.parameters.RequestBody(
                        description = "Login credentials (email/username and password)",
                        required = true,
                        content = @Content(schema = @Schema(implementation = LoginDTO.class))
                )
                @RequestBody @Valid LoginDTO loginDTO) {
            ResponseDTO<?> login = userService.login(loginDTO);
            return ResponseEntity.ok(login);
        }

        @Operation(
                summary = "User registration",
                description = "Registers a new user and sends an email verification link."
        )
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Successfully registered",
                        content = @Content(mediaType = "application/json",
                                schema = @Schema(implementation = RegistrationResponseDTO.class))),
                @ApiResponse(responseCode = "400", description = "Validation failed / Bad request"),
                @ApiResponse(responseCode = "409", description = "Conflict: Email or username already exists")
        })
        @PostMapping("/register")
        public ResponseEntity<ResponseDTO<?>> register(
                @io.swagger.v3.oas.annotations.parameters.RequestBody(
                        description = "User registration details",
                        required = true,
                        content = @Content(schema = @Schema(implementation = UserRegisterRequestDTO.class))
                )
                @RequestBody @Valid UserRegisterRequestDTO requestDTO,
                HttpServletRequest httpServletRequest) {

            RegistrationResponseDTO register = userService.register(requestDTO, httpServletRequest);
            return ResponseEntity.ok(ResponseDTO.success(register));
        }

        @Operation(
                summary = "Verify user account",
                description = "Verifies a user's email using the token sent to their email address."
        )
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Account successfully verified"),
                @ApiResponse(responseCode = "400", description = "Invalid or expired token")
        })
        @GetMapping("/verify")
        public ResponseEntity<String> verifyAccount(
                @Parameter(description = "Verification token sent via email", example = "abcd1234xyz")
                @RequestParam("token") String token) {
            userService.verifyAccount(token);
            return ResponseEntity.ok("Account successfully verified! You can now log in.");
        }
    }
