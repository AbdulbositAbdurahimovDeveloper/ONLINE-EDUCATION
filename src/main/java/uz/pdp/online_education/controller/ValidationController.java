package uz.pdp.online_education.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.online_education.payload.AvailabilityDTO;
import uz.pdp.online_education.service.interfaces.ValidationService;

@RestController
@RequestMapping("/api/open/v1/validation")
@RequiredArgsConstructor
@Tag(name = "Validation Controller", description = "APIs for validating username and email availability")
public class ValidationController {

    private final ValidationService validationService;

    @GetMapping("/username/check")
    @Operation(summary = "Check username availability", description = "Verify if a given username is available")
    @ApiResponse(responseCode = "200", description = "Username availability checked successfully")
    public ResponseEntity<AvailabilityDTO> checkUsernameAvailability(@RequestParam String username) {
        boolean isAvailable = validationService.isUsernameAvailable(username);
        return ResponseEntity.ok(new AvailabilityDTO(isAvailable));
    }

    @GetMapping("/email/check")
    @Operation(summary = "Check email availability", description = "Verify if a given email is available")
    @ApiResponse(responseCode = "200", description = "Email availability checked successfully")
    public ResponseEntity<AvailabilityDTO> checkEmailAvailability(@RequestParam String email) {
        boolean isAvailable = validationService.isEmailAvailable(email);
        return ResponseEntity.ok(new AvailabilityDTO(isAvailable));
    }
}
