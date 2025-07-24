package uz.pdp.online_education.assembler;

import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;
import uz.pdp.online_education.controller.CourseController;
import uz.pdp.online_education.mapper.CourseMapper;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.payload.course.CourseDetailDTO;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CourseModelAssembler extends RepresentationModelAssemblerSupport<Course, CourseDetailDTO> {

    private final CourseMapper courseMapper;

    public CourseModelAssembler(CourseMapper courseMapper) {
        super(CourseController.class, CourseDetailDTO.class);
        this.courseMapper = courseMapper;
    }

    /**
     * @param course
     * @return
     */
    @Override
    public CourseDetailDTO toModel(Course course) {
        CourseDetailDTO courseDetailDTO = courseMapper.courseToCourseDetailDTO(course);

        courseDetailDTO.add(linkTo(methodOn(CourseController.class)
                .read(course.getId()))
                .withSelfRel());

        return courseDetailDTO;
    }
}
