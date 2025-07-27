package uz.pdp.online_education.service;

import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.online_education.mapper.CourseMapper;
import uz.pdp.online_education.model.Category;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.model.Module;
import uz.pdp.online_education.payload.course.CourseDetailDTO;
import uz.pdp.online_education.repository.CategoryRepository;
import uz.pdp.online_education.repository.CourseRepository;
import uz.pdp.online_education.repository.UserRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class GenerateService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    // Faker obyekti realistik ma'lumotlar yaratish uchun
    private final Faker faker = new Faker(new Locale("en-US")); // Ingliz tilida ma'lumotlar
    private final CourseMapper courseMapper;

    @Transactional // Barcha operatsiyalar bitta tranzaksiyada bajariladi
    public List<CourseDetailDTO> generateCoursesAndModules(int count) {
        
        // Agar bazada o'qituvchi va kategoriya bo'lmasa, test uchun yaratib olamiz
        User instructor = getOrCreateTestInstructor();
        Category category = getOrCreateTestCategory();
        
        List<Course> coursesToSave = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            // --- Kurs Yaratish ---
            Course course = new Course();
            String courseTitle = faker.educator().course() + " - " + faker.number().digits(4);
            course.setTitle(courseTitle);
            course.setDescription(faker.lorem().paragraph(3));
            course.setSlug(courseTitle.toLowerCase().replace(" ", "-"));
            course.setInstructor(instructor);
            course.setCategory(category);
            // thumbnailUrl'ni hozircha null qoldiramiz

            // --- Modullar Yaratish ---
            List<Module> modules = new ArrayList<>();
            for (int j = 0; j < 5; j++) {
                Module module = new Module();
                module.setTitle("Module " + (j + 1) + ": " + faker.lorem().sentence(3));
                module.setDescription(faker.lorem().paragraph(2));long priceInSom = faker.number().numberBetween(500_000L, 1_500_000L);
                long priceInTiyin = priceInSom * 100;
                module.setPrice(priceInTiyin); // Agar price tiyinda saqlansa

                module.setOrderIndex(j);
                
                // Eng muhim qadam: Modulni kursga bog'lash
                module.setCourse(course);
                
                modules.add(module);
            }
            
            // Kursga modullar ro'yxatini o'rnatamiz
            course.setModules(modules);
            
            coursesToSave.add(course);
        }

        // Barcha yaratilgan kurslarni bitta so'rovda saqlaymiz (cascade bo'lgani uchun modullar ham saqlanadi)
        courseRepository.saveAll(coursesToSave);

        return coursesToSave.stream().map(courseMapper::courseToCourseDetailDTO).toList();

    }

    // Yordamchi metodlar
    private User getOrCreateTestInstructor() {
        // "instructor" nomli user'ni qidiramiz, agar yo'q bo'lsa, yaratamiz
        return userRepository.findByUsername("admin")
                .orElseGet(() -> {
                    User user = new User();
                    user.setUsername("instructor");
                    user.setPassword("$2a$10$YQyK8a1BsPQFXgMLkPG5F.xjtaSZdSYqrK6EElOOQiP4T1LD3JuDG"); // Parolni shifrlash kerak aslida
                    user.setRole(uz.pdp.online_education.enums.Role.INSTRUCTOR);
                    return userRepository.save(user);
                });
    }

    private Category getOrCreateTestCategory() {
        return categoryRepository.findByName("Programming")
                .orElseGet(() -> {
                    Category category = new Category();
                    category.setName("Programming");
                    category.setSlug("programming");
                    return categoryRepository.save(category);
                });
    }
}