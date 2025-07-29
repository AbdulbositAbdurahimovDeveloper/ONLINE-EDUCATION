package uz.pdp.online_education.service;

import com.github.slugify.Slugify;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.online_education.exceptions.DataConflictException;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.mapper.CourseMapper;
import uz.pdp.online_education.model.Attachment;
import uz.pdp.online_education.model.Category;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.model.Module;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.payload.PageDTO;
import uz.pdp.online_education.payload.course.CourseCreateDTO;
import uz.pdp.online_education.payload.course.CourseDetailDTO;
import uz.pdp.online_education.payload.course.CourseUpdateDTO;
import uz.pdp.online_education.repository.AttachmentRepository;
import uz.pdp.online_education.repository.CategoryRepository;
import uz.pdp.online_education.repository.CourseRepository;
import uz.pdp.online_education.repository.ModuleRepository;
import uz.pdp.online_education.service.interfaces.CourseService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;
    private final Slugify slugify = Slugify.builder().build();
    private final AttachmentRepository attachmentRepository;
    private final CategoryRepository categoryRepository;
    private final ModuleRepository moduleRepository;


    /**
     * @param page default:0
     * @param size default:10
     * @return page
     */
    @Override
    public PageDTO<CourseDetailDTO> read(Integer page, Integer size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Course> courses = courseRepository.findAllOrderByAverageRatingDesc(pageRequest);

        return new PageDTO<>(
                courses.getContent().stream().map(courseMapper::courseToCourseDetailDTO).toList(),
                courses.getNumber(),
                courses.getSize(),
                courses.getTotalElements(),
                courses.getTotalPages(),
                courses.isLast(),
                courses.isFirst(),
                courses.getNumberOfElements(),
                courses.isEmpty()
        );
    }

    /**
     * @param id
     * @return
     */
    @Override
    public CourseDetailDTO read(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + id));

        return courseMapper.courseToCourseDetailDTO(course);
    }

    /**
     * @param courseCreateDTO courseCreateDTO
     * @param instructor
     * @return courseDetailDTO
     */
    @Override
    @Transactional
    public CourseDetailDTO create(CourseCreateDTO courseCreateDTO, User instructor) {

        String baseSlug = slugify.slugify(courseCreateDTO.getTitle());

        Attachment attachment = attachmentRepository.findById(courseCreateDTO.getThumbnailId())
                .orElseThrow(() -> new EntityNotFoundException("Attachment not found with id: " + courseCreateDTO.getThumbnailId()));

        Category category = categoryRepository.findById(courseCreateDTO.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + courseCreateDTO.getCategoryId()));


        Course course = new Course();
        course.setTitle(courseCreateDTO.getTitle());
        course.setDescription(courseCreateDTO.getDescription());
        course.setSlug(baseSlug);
        course.setThumbnailUrl(attachment);
        course.setInstructor(instructor);
        course.setCategory(category);

        courseRepository.save(course);
        return courseMapper.courseToCourseDetailDTO(course);
    }

    /**
     * @param id              Long
     * @param courseUpdateDTO CourseUpdateDTO
     * @param instructor      User
     * @return courseDetailDTO
     */
    @Override
    @Transactional
    public CourseDetailDTO update(Long id, CourseUpdateDTO courseUpdateDTO, User instructor) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + id));

        Category category = categoryRepository.findById(courseUpdateDTO.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + courseUpdateDTO.getCategoryId()));

        String baseSlug = slugify.slugify(courseUpdateDTO.getTitle());

        course.setTitle(courseUpdateDTO.getTitle());
        course.setSlug(baseSlug);
        course.setDescription(courseUpdateDTO.getDescription());
        course.setInstructor(instructor);
        course.setCategory(category);

        courseRepository.save(course);
        return courseMapper.courseToCourseDetailDTO(course);
    }

    /**
     * Toggles the 'success' status of a course.
     * To be marked as 'true', a course must meet all publication criteria:
     * 1. It must have at least one module.
     * 2. Every module must have at least one lesson.
     * 3. Every lesson must have at least one content block.
     *
     * @param id The ID of the course to update.
     */
    @Override
    @Transactional
    public void updateSuccess(Long id) {

        // 1. Kursni bazadan topamiz.
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + id));

        // 2. Agar kurs allaqachon "muvaffaqiyatli" bo'lsa, uni shunchaki "muvaffaqiyatsiz" qilamiz.
        if (course.isSuccess()) {
            course.setSuccess(false);
            courseRepository.save(course);
            return; // Metod ishini shu yerda yakunlaymiz.
        }

        // 3. Agar "muvaffaqiyatli" qilmoqchi bo'lsak, chuqur tekshiruvni boshlaymiz.
        List<Module> modules = course.getModules();

        // 3a. Birinchi darajali tekshiruv: Kursda modullar bormi?
        if (modules == null || modules.isEmpty()) {
            throw new DataConflictException("Cannot publish: The course has no modules.");
        }

        // 3b. Ikkinchi darajali tekshiruv: Barcha modullarda darslar bormi?
        boolean allModulesHaveLessons = modules.stream()
                .allMatch(module -> module.getLessons() != null && !module.getLessons().isEmpty());

        if (!allModulesHaveLessons) {
            throw new DataConflictException("Cannot publish: All modules must contain at least one lesson.");
        }

        // 3c. UCHINCHI VA ENG CHUQUR DARAJALI TEKSHIRUV:
        // Barcha modullardagi barcha darslarda kontent bormi?
        boolean allLessonsHaveContent = modules.stream() // Barcha modullar bo'yicha...
                .allMatch(module -> // Har bir modulning...
                        module.getLessons().stream() // Barcha darslari bo'yicha...
                                .allMatch(lesson -> // Har bir darsning...
                                        lesson.getContents() != null && !lesson.getContents().isEmpty() // Kontenti borligini tekshiramiz.
                                )
                );

        // 4. Yakuniy qaror
        if (allLessonsHaveContent) {
            course.setSuccess(true);
        } else {
            // Agar birorta darsda kontent bo'lmasa, xatolik beramiz.
            throw new DataConflictException("Cannot publish: All lessons in all modules must contain at least one piece of content.");
        }

        // 5. O'zgarishlarni saqlaymiz.
        courseRepository.save(course);
    }

    /**
     * @param id Long
     */
    @Override
    @Transactional
    public void delete(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + id));

        if (course.getModules() != null) {
            moduleRepository.deleteAll(course.getModules());
        }

        courseRepository.delete(course);
    }


}
