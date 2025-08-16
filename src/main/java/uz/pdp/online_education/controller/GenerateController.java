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

        /**
         * Generates a list of demo courses with modules.
         *
         * @param count number of courses to generate (max 100)
         * @return list of generated CourseDetailDTO
         */
        @GetMapping("/course/{count}")
        public ResponseEntity<ResponseDTO<?>> generateCourse(@PathVariable int count) {
            if (count > 100) {
                count = 100; // Limit to prevent excessive generation
            }

            List<CourseDetailDTO> courseDetailDTOS = generateService.generateCoursesAndModules(count);
            return ResponseEntity.ok(ResponseDTO.success(courseDetailDTOS));
        }

        /**
         * Generates demo full-stack courses with modules and contents.
         *
         * @param count number of full courses to generate
         * @return success response
         */
        @GetMapping("/full-course/{count}")
        public ResponseEntity<ResponseDTO<?>> generateCoursesAndModules(@PathVariable int count) {
            generateService.generateFullStackCourses(count);
            return ResponseEntity.ok(ResponseDTO.success("Full courses generated successfully"));
        }

        /**
         * Generates demo student users.
         *
         * @param count number of students to generate
         * @return success response
         */
        @GetMapping("/user/student/{count}")
        public ResponseEntity<ResponseDTO<?>> generateStudents(@PathVariable int count) {
            generateService.generateUserStudent(count);
            return ResponseEntity.ok(ResponseDTO.success("Students generated successfully"));
        }

        /**
         * Generates demo payments for students.
         *
         * @return success response
         */
        @GetMapping("/payment")
        public ResponseEntity<ResponseDTO<?>> generatePayments() {
            generateService.generateStudentPayments();
            return ResponseEntity.ok(ResponseDTO.success("Payments generated successfully"));
        }

        /**
         * Generates demo course reviews.
         *
         * @return success response
         */
        @GetMapping("/review")
        public ResponseEntity<ResponseDTO<?>> generateCourseReviews() {
            generateService.generateCourseReviews();
            return ResponseEntity.ok(ResponseDTO.success("Course reviews generated successfully"));
        }
    }
