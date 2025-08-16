package uz.pdp.online_education.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.online_education.payload.ResponseDTO;
import uz.pdp.online_education.payload.course.CourseDetailDTO;
import uz.pdp.online_education.service.GenerateService;

import java.util.List;

@RestController
@RequestMapping("/api/generate")
@RequiredArgsConstructor
@Tag(name = "Data Generation", description = "Endpoints to generate demo data for courses, students, payments, and reviews")
public class GenerateController {

    private final GenerateService generateService;

    @Operation(
            summary = "Generate demo courses",
            description = "Generates a list of demo courses with modules. Max 100 courses per request.",
            parameters = @Parameter(name = "count", description = "Number of courses to generate", example = "10"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Demo courses generated successfully",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                                            {
                                              "success": true,
                                              "data": [
                                                {"id": 1, "title": "Java Basics", "modules": []},
                                                {"id": 2, "title": "Spring Boot Intro", "modules": []}
                                              ]
                                            }
                                            """)))
            }
    )
    @GetMapping("/course/{count}")
    public ResponseEntity<ResponseDTO<?>> generateCourse(@PathVariable int count) {
        if (count > 100) count = 100;
        List<CourseDetailDTO> courseDetailDTOS = generateService.generateCoursesAndModules(count);
        return ResponseEntity.ok(ResponseDTO.success(courseDetailDTOS));
    }

    @Operation(summary = "Generate full-stack courses",
            description = "Generates demo full-stack courses with modules and contents.",
            parameters = @Parameter(name = "count", description = "Number of full courses to generate", example = "5"),
            responses = @ApiResponse(responseCode = "200", description = "Full courses generated successfully")
    )
    @GetMapping("/full-course/{count}")
    public ResponseEntity<ResponseDTO<?>> generateCoursesAndModules(@PathVariable int count) {
        generateService.generateFullStackCourses(count);
        return ResponseEntity.ok(ResponseDTO.success("Full courses generated successfully"));
    }

    @Operation(summary = "Generate demo student users",
            parameters = @Parameter(name = "count", description = "Number of students to generate", example = "20"),
            responses = @ApiResponse(responseCode = "200", description = "Students generated successfully")
    )
    @GetMapping("/user/student/{count}")
    public ResponseEntity<ResponseDTO<?>> generateStudents(@PathVariable int count) {
        generateService.generateUserStudent(count);
        return ResponseEntity.ok(ResponseDTO.success("Students generated successfully"));
    }

    @Operation(summary = "Generate demo student payments",
            description = "Generates demo payments for all students",
            responses = @ApiResponse(responseCode = "200", description = "Payments generated successfully")
    )
    @GetMapping("/payment")
    public ResponseEntity<ResponseDTO<?>> generatePayments() {
        generateService.generateStudentPayments();
        return ResponseEntity.ok(ResponseDTO.success("Payments generated successfully"));
    }

    @Operation(summary = "Generate demo course reviews",
            description = "Generates demo reviews for courses",
            responses = @ApiResponse(responseCode = "200", description = "Course reviews generated successfully")
    )
    @GetMapping("/review")
    public ResponseEntity<ResponseDTO<?>> generateCourseReviews() {
        generateService.generateCourseReviews();
        return ResponseEntity.ok(ResponseDTO.success("Course reviews generated successfully"));
    }
}
