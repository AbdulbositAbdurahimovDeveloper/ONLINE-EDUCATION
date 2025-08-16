    package uz.pdp.online_education.controller;

    import io.swagger.v3.oas.annotations.Operation;
    import io.swagger.v3.oas.annotations.Parameter;
    import io.swagger.v3.oas.annotations.responses.ApiResponse;
    import io.swagger.v3.oas.annotations.responses.ApiResponses;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.access.prepost.PreAuthorize;
    import org.springframework.web.bind.annotation.*;
    import uz.pdp.online_education.payload.ResponseDTO;
    import uz.pdp.online_education.service.interfaces.AnswerOptionService;

    @RestController
    @RequestMapping("/api/v1/options")
    @RequiredArgsConstructor
    public class AnswerOptionController {

        private final AnswerOptionService answerOptionService;

        /**
         * Deletes an answer option by ID.
         * Accessible only to ADMIN and INSTRUCTOR roles.
         */

        @Operation(
                summary = "Delete answer option",
                description = "Deletes a quiz answer option by its ID. " +
                        "This operation is restricted to users with ADMIN or INSTRUCTOR roles."
        )
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Answer option deleted successfully"),
                @ApiResponse(responseCode = "403", description = "You are not authorized to perform this action"),
                @ApiResponse(responseCode = "404", description = "Answer option with the given ID was not found")
        })
        @DeleteMapping("/{optionId}")
        @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
        public ResponseEntity<ResponseDTO<?>> deleteAnswerOption(
                @Parameter(description = "ID of the answer option to be deleted", example = "5")
                @PathVariable Long optionId
        )
        {
            answerOptionService.delete(optionId);
            return ResponseEntity.ok(ResponseDTO.success("Answer option deleted successfully"));
        }

    }
