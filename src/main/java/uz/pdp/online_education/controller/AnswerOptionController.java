package uz.pdp.online_education.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.pdp.online_education.payload.ResponseDTO;
import uz.pdp.online_education.payload.quiz.AnswerOptionResponseDTO;
import uz.pdp.online_education.service.interfaces.AnswerOptionService;

@RestController
@RequestMapping("/api/v1/options")
@RequiredArgsConstructor
public class AnswerOptionController {

    private final AnswerOptionService answerOptionService;


    @DeleteMapping("/{optionId}")
    @PreAuthorize(value = "hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDTO<?>> deleteAnswerOption(@PathVariable Long optionId) {
        answerOptionService.delete(optionId);
        return ResponseEntity.ok(ResponseDTO.success("Answer option deleted successfully"));
    }

}
