package uz.pdp.online_education.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.online_education.payload.ResponseDTO;
import uz.pdp.online_education.payload.errors.ErrorDTO;
import uz.pdp.online_education.payload.quiz.AnswerOptionCreatDTO;
import uz.pdp.online_education.payload.quiz.AnswerOptionResponseDTO;
import uz.pdp.online_education.service.interfaces.AnswerOptionService;

@RestController
@RequestMapping("/api/v1/questions/{questionId}/options")
@RequiredArgsConstructor
public class QuestionAnswerOptionController {

    private final AnswerOptionService answerOptionService;

    @PostMapping
    public ResponseEntity<ResponseDTO<?>> createAnswerOption(@PathVariable Long questionId,
                                                             @RequestBody @Valid AnswerOptionCreatDTO createDTO) {
        if (!createDTO.getQuestionId().equals(questionId)) {
            return ResponseEntity.badRequest().body(ResponseDTO.error(new ErrorDTO(
                    HttpStatus.BAD_REQUEST.value(),
                    "The question ID in the request body does not match the path variable."
            )));
        }
        AnswerOptionResponseDTO createdOption = answerOptionService.create(createDTO);
        return ResponseEntity.ok(ResponseDTO.success(createdOption));
    }
}
