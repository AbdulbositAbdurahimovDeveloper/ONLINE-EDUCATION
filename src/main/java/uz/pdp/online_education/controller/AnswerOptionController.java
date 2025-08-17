package uz.pdp.online_education.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.pdp.online_education.payload.ResponseDTO;
import uz.pdp.online_education.service.interfaces.AnswerOptionService;

@RestController
@RequestMapping("/api/v1/options")
@RequiredArgsConstructor
@Tag(name = "Answer Option API", description = "API endpoints for managing quiz answer options")
public class AnswerOptionController {

    private final AnswerOptionService answerOptionService;

    @Operation(
            summary = "Delete answer option",
            description = "Deletes a quiz answer option by its ID. " +
                    "Only ADMIN and INSTRUCTOR roles are allowed to perform this operation."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Answer option deleted successfully",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "message": "Answer option deleted successfully",
                                      "data": null
                                    }
                                    """))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not authorized",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "You are not authorized to perform this action"
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Not Found - Answer option not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "Answer option with ID=5 not found"
                                    }
                                    """)))
    })
    @DeleteMapping("/{optionId}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDTO<?>> deleteAnswerOption(
            @Parameter(description = "ID of the answer option to be deleted", example = "5")
            @PathVariable Long optionId
    ) {
        answerOptionService.delete(optionId);
        return ResponseEntity.ok(ResponseDTO.success("Answer option deleted successfully"));
    }

}
