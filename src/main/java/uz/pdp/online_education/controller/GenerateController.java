package uz.pdp.online_education.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.pdp.online_education.payload.ResponseDTO;
import uz.pdp.online_education.payload.course.CourseDetailDTO;
import uz.pdp.online_education.service.GenerateService;

import java.util.List;

@RestController
@RequestMapping("/api/generate")
@RequiredArgsConstructor
public class GenerateController {

    private final GenerateService generateService;

    @GetMapping("/course/{count}")
    public ResponseEntity<ResponseDTO<?>> generateCourse(@PathVariable int count) {

        if (count > 100) {
            count = 100;
        }

        List<CourseDetailDTO> courseDetailDTOS = generateService.generateCoursesAndModules(count);

        return ResponseEntity.ok(ResponseDTO.success(courseDetailDTOS));
    }

    @GetMapping("/full-course/{count}")
    public ResponseEntity<ResponseDTO<?>> generateCoursesAndModules(@PathVariable int count) {
        generateService.generateFullStackCourses(count);
        return ResponseEntity.ok(ResponseDTO.success("created"));
    }

    @GetMapping("user/student/{count}")
    public ResponseEntity<ResponseDTO<?>> generateStudents(@PathVariable int count) {
        generateService.generateUserStudent(count);
        return ResponseEntity.ok(ResponseDTO.success("created"));
    }

    @GetMapping("payment")
    public ResponseEntity<ResponseDTO<?>> generatePayments() {
        generateService.generateStudentPayments();
        return ResponseEntity.ok(ResponseDTO.success("created"));
    }
    @GetMapping("review")
    public ResponseEntity<ResponseDTO<?>> generateCourseReviews() {
        generateService.generateCourseReviews();
        return ResponseEntity.ok(ResponseDTO.success("created"));
    }


}
