package uz.pdp.online_education.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.online_education.payload.ResponseDTO;
import uz.pdp.online_education.payload.user.LoginDTO;
import uz.pdp.online_education.payload.user.RegistrationResponseDTO;
import uz.pdp.online_education.payload.user.UserRegisterRequestDTO;
import uz.pdp.online_education.service.interfaces.UserService;

@Tag(name = "Authentication API", description = "Endpoints related to authentication, registration and verification of users.")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Operation(
            summary = "User login",
            description = """
                    Authenticate a user with **email/username** and **password**.
                    <br>
                    Returns an access token and user details if authentication succeeds.
                    <br>
                    ⚠️ Token must be used in `Authorization: Bearer <token>` header for subsequent requests.
                    """,
            tags = {"Authentication", "Login"},
            requestBody = @RequestBody(
                    description = "Login credentials (email/username and password)",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = LoginDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Login with username",
                                            value = "{ \"username\": \"john_doe\", \"password\": \"secret123\" }"
                                    ),
                                    @ExampleObject(
                                            name = "Login with email",
                                            value = "{ \"username\": \"john@example.com\", \"password\": \"mypassword\" }"
                                    )
                            }
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully logged in",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid login request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Incorrect credentials"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/login")
    public ResponseEntity<ResponseDTO<?>> login(@Valid @RequestBody LoginDTO loginDTO) {
        ResponseDTO<?> login = userService.login(loginDTO);
        return ResponseEntity.ok(login);
    }

    @Operation(
            summary = "User registration",
            description = """
                    Registers a new user and sends an **email verification link**.
                    <br>
                    After registration, the user must check their email and confirm before logging in.
                    """,
            tags = {"Authentication", "Registration"},
            requestBody = @RequestBody(
                    description = "User registration details",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserRegisterRequestDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Basic registration",
                                            value = "{ \"firstName\": \"John\", \"lastName\": \"Doe\", \"username\": \"john_doe\", \"email\": \"john@example.com\", \"password\": \"secret123\" }"
                                    )
                            }
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully registered",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RegistrationResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed / Bad request"),
            @ApiResponse(responseCode = "409", description = "Conflict: Email or username already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/register")
    public ResponseEntity<ResponseDTO<?>> register(
            @Valid @RequestBody UserRegisterRequestDTO requestDTO,
            @Parameter(description = "Servlet request object used to extract additional client details", hidden = true)
            HttpServletRequest httpServletRequest) {

        RegistrationResponseDTO register = userService.register(requestDTO, httpServletRequest);
        return ResponseEntity.ok(ResponseDTO.success(register));
    }

    @Operation(
            summary = "Verify user account",
            description = """
                    Verifies a user's email using the token sent to their email address.
                    <br>
                    Token is usually valid for **15 minutes**.
                    """,
            tags = {"Authentication", "Verification"},
            parameters = {
                    @Parameter(
                            name = "token",
                            in = ParameterIn.QUERY,
                            required = true,
                            description = "Verification token received via email",
                            example = "abcd1234xyz"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account successfully verified"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/verify")
    public ResponseEntity<String> verifyAccount(@RequestParam("token") String token) {
        userService.verifyAccount(token);
        return ResponseEntity.ok("Account successfully verified! You can now log in.");
    }
}
