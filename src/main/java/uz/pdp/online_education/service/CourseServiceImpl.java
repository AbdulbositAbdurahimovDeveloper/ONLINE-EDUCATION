package uz.pdp.online_education.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.mapper.CourseMapper;
import uz.pdp.online_education.model.Attachment;
import uz.pdp.online_education.model.Category;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.payload.CourseRequestDTO;
import uz.pdp.online_education.payload.CourseResponseDTO;
import uz.pdp.online_education.repository.AttachmentRepository;
import uz.pdp.online_education.repository.CategoryRepository;
import uz.pdp.online_education.repository.CourseRepository;
import uz.pdp.online_education.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final AttachmentRepository attachmentRepository;
    private final CourseMapper courseMapper;

    @Override
    public CourseResponseDTO create(CourseRequestDTO dto) {
        Course course = new Course();
        courseMapper.updateCourseFromDto(dto, course);
        course.setInstructor(getUser(dto.getInstructorId()));
        course.setCategory(getCategory(dto.getCategoryId()));
        course.setThumbnailUrl(getAttachment(dto.getThumbnailId()));

        Course saved = courseRepository.save(course);
        log.info("Created Course with id={}", saved.getId());
        return courseMapper.toDto(saved);
    }

    @Override
    public CourseResponseDTO update(Long id, CourseRequestDTO dto) {
        Course course = getCourse(id);
        courseMapper.updateCourseFromDto(dto, course);
        course.setInstructor(getUser(dto.getInstructorId()));
        course.setCategory(getCategory(dto.getCategoryId()));
        course.setThumbnailUrl(getAttachment(dto.getThumbnailId()));

        Course updated = courseRepository.save(course);
        log.info("Updated Course id={}", updated.getId());
        return courseMapper.toDto(updated);
    }

    @Override
    public void delete(Long id) {
        Course course = getCourse(id);
        courseRepository.delete(course);
        log.warn("Deleted Course id={}", id);
    }

    @Override
    public CourseResponseDTO getById(Long id) {
        Course course = getCourse(id);
        return courseMapper.toDto(course);
    }

    @Override
    public List<CourseResponseDTO> getAll() {
        return courseRepository.findAll()
                .stream()
                .map(courseMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseResponseDTO> getByCategoryId(Long categoryId) {
        return courseRepository.findAllByCategoryId(categoryId)
                .stream()
                .map(courseMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseResponseDTO> getByInstructorId(Long instructorId) {
        return courseRepository.findAllByInstructorId(instructorId)
                .stream()
                .map(courseMapper::toDto)
                .collect(Collectors.toList());
    }

    // ========== PRIVATE HELPERS ==========
    private Course getCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + id));
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    private Category getCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));
    }

    private Attachment getAttachment(Long id) {
        return attachmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Attachment not found with id: " + id));
    }
}
