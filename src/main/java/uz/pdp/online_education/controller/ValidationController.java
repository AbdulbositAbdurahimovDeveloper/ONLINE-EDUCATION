package uz.pdp.online_education.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.online_education.payload.AvailabilityDTO;
import uz.pdp.online_education.service.ValidationService;

@RestController
@RequestMapping("/api/open/v1/validation")
@RequiredArgsConstructor
public class ValidationController {

    private final ValidationService validationService;

    @GetMapping("/username/check")
    public ResponseEntity<AvailabilityDTO> checkUsernameAvailability(@RequestParam String username) {
        boolean isAvailable = validationService.isUsernameAvailable(username);
        return ResponseEntity.ok(new AvailabilityDTO(isAvailable));
    }

    @GetMapping("/email/check")
    public ResponseEntity<AvailabilityDTO> checkEmailAvailability(@RequestParam String email) {
        boolean isAvailable = validationService.isEmailAvailable(email);
        return ResponseEntity.ok(new AvailabilityDTO(isAvailable));
    }
}