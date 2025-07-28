package uz.pdp.online_education.service;

import com.github.javafaker.Faker;
import com.github.slugify.Slugify;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.online_education.enums.QuestionType;
import uz.pdp.online_education.enums.Role;
import uz.pdp.online_education.exceptions.DataConflictException;
import uz.pdp.online_education.mapper.CourseMapper;
import uz.pdp.online_education.model.*;
import uz.pdp.online_education.model.Module;
import uz.pdp.online_education.model.lesson.Content;
import uz.pdp.online_education.model.lesson.Lesson;
import uz.pdp.online_education.model.lesson.QuizContent;
import uz.pdp.online_education.model.lesson.TextContent;
import uz.pdp.online_education.model.quiz.AnswerOption;
import uz.pdp.online_education.model.quiz.Question;
import uz.pdp.online_education.model.quiz.Quiz;
import uz.pdp.online_education.payload.course.CourseDetailDTO;
import uz.pdp.online_education.payload.payment.PaymentCreateDTO;
import uz.pdp.online_education.payload.review.ReviewCreateDTO;
import uz.pdp.online_education.repository.*;
import uz.pdp.online_education.service.interfaces.PaymentService;
import uz.pdp.online_education.service.interfaces.ReviewService;

import java.util.*;

/**
 * Service class responsible for generating realistic test data for the application.
 * It can create a full stack of entities: Courses, Modules, Lessons, and all related content.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GenerateService  {

    //<editor-fold desc="Dependencies">
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final CategoryRepository categoryRepository;
    private final ModuleRepository moduleRepository;
    private final QuizRepository quizRepository;
    private final PaymentRepository paymentRepository;
    private final ReviewService reviewService;
    private final CourseMapper courseMapper;
    private final PaymentService paymentService;
    private final PasswordEncoder passwordEncoder;
    //</editor-fold>

    //<editor-fold desc="Helper Tools and Constants">
    private final Faker faker = new Faker(new Locale("en-US"));
    private final Slugify slugify = Slugify.builder().build();
    private final Random random = new Random();

    private static final List<String> INSTRUCTOR_USERNAMES = Arrays.asList(
            "prof.alimov", "sarah.jenkins", "coding_guru_aziz", "design.master.davis",
            "math_expert_uz", "elena.petrova", "kenji.tanaka_dev", "dr_michael_chen",
            "laura.sullivan_teaches", "ali.valiyev92"
    );

    private static final List<String> CATEGORY_NAMES = List.of(
            "Information Technology", "Software Development", "Cybersecurity", "Data Science",
            "Digital Marketing", "Project Management", "Business Analysis", "Graphic Design",
            "Human Resources", "Accounting & Finance"
    );

    private static final List<Long> PREDEFINED_PRICES_IN_SOM = Arrays.asList(
            420_000L, 500_000L, 700_000L, 1_400_000L, 1_500_000L
    );
    //</editor-fold>

    @Transactional
    public void generateFullStackCourses(int count) {
        log.info("Starting full-stack course generation process for {} courses.", count);

        Set<String> existingCourseTitles = courseRepository.findAllTitles();
        Set<String> existingCourseSlugs = courseRepository.findAllSlugs();
        Set<String> existingModuleTitles = moduleRepository.findAllTitles();
        Set<String> existingQuizTitles = quizRepository.findAllTitles();

        log.info("Fetched existing identifiers (Course Titles: {}, Slugs: {}, Module Titles: {}, Quiz Titles: {})",
                existingCourseTitles.size(), existingCourseSlugs.size(), existingModuleTitles.size(), existingQuizTitles.size());

        List<Course> coursesToSave = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            log.debug("--- Generating Course #{} of {} ---", (i + 1), count);
            String instructorUsername = INSTRUCTOR_USERNAMES.get(random.nextInt(INSTRUCTOR_USERNAMES.size()));
            User instructor = getOrCreateUser(instructorUsername);
            String categoryName = CATEGORY_NAMES.get(random.nextInt(CATEGORY_NAMES.size()));
            Category category = getOrCreateCategory(categoryName);
            Course course = createCourse(existingCourseTitles, existingCourseSlugs, instructor, category);
            course.setModules(generateModulesForCourse(course, existingModuleTitles, existingQuizTitles));
            coursesToSave.add(course);
        }

        log.info("Saving {} new courses and their related entities to the database...", coursesToSave.size());
        courseRepository.saveAll(coursesToSave);
        log.info("Successfully generated and saved {} courses.",count);
    }

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
                module.setDescription(faker.lorem().paragraph(2));
                long priceInSom = faker.number().numberBetween(500_000L, 1_500_000L);
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

    // Bu metodni GenerateService klassingizga qo'shing

    @Transactional
    public void generateUserStudent(int count) {
        log.info("Starting generation of {} student users...", count);

        // 1. Fetch existing unique constraints to avoid database errors
        Set<String> existingUsernames = userRepository.findAllUsernames(); // Bu metodni repository'ga qo'shish kerak
        Set<String> existingEmails = userProfileRepository.findAllEmails();   // Bu metodni repository'ga qo'shish kerak
        log.debug("Found {} existing usernames and {} existing emails.", existingUsernames.size(), existingEmails.size());

        List<User> usersToSave = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            // --- Asosiy obyektlarni yaratish ---
            User newUser = new User();
            UserProfile profile = new UserProfile();

            // --- Ma'lumotlarni generatsiya qilish ---
            String firstName = faker.name().firstName();
            String lastName = faker.name().lastName();

            // 2. Generate unique username and email
            String username = generateUniqueUsername(existingUsernames, firstName, lastName);
            String email = generateUniqueEmail(existingEmails, username);

            // --- UserProfile'ni to'ldirish ---
            profile.setFirstName(firstName);
            profile.setLastName(lastName);
            profile.setEmail(email);
            profile.setPhoneNumber(faker.phoneNumber().cellPhone());
            profile.setBio(faker.lorem().sentence(10)); // Talaba uchun qisqa bio

            // --- User'ni to'ldirish ---
            newUser.setUsername(username);
            newUser.setPassword(passwordEncoder.encode("123")); // Barcha studentlar uchun standart parol
            newUser.setRole(Role.STUDENT); // Rolni STUDENT qilib belgilaymiz
            newUser.setEnabled(true); // Foydalanuvchi aktiv holatda

            // 3. Establish the bidirectional relationship
            newUser.setProfile(profile);
            profile.setUser(newUser);

            usersToSave.add(newUser);
            log.debug("Prepared student user #{} for saving. Username: {}", (i + 1), username);
        }



        // 4. Save all new users in a single batch operation
        if (!usersToSave.isEmpty()) {
            userRepository.saveAll(usersToSave);
            log.info("Successfully generated and saved {} new student users.", usersToSave.size());
        } else {
            log.warn("No new student users were generated.");
        }
    }

    // Bu metodni GenerateService klassingizga qo'shing

    @Transactional // Bu operatsiya uzoq davom etishi mumkinligi uchun @Transactional shart
    public void generateStudentPayments() {
        log.info("Starting generation of payments for student users...");

        // 1. Get all students and all modules from the database
        List<User> students = userRepository.findAllByRole(Role.STUDENT); // Bu metodni repository'ga qo'shish kerak
        List<Module> allModules = moduleRepository.findAll();

        if (students.isEmpty() || allModules.isEmpty()) {
            log.warn("Cannot generate payments. No students or modules found in the database.");
            return;
        }

        log.info("Found {} students and {} modules. Starting payment simulation.", students.size(), allModules.size());
        int totalPaymentsCreated = 0;
        int totalErrors = 0;

        // 2. Iterate through each student to generate payments
        for (User student : students) {
            log.debug("Processing payments for student: {}", student.getUsername());

            // Modullarni aralashtirib yuboramiz, toki har safar har xil modul sotib olinsin
            List<Module> shuffledModules = new ArrayList<>(allModules);
            Collections.shuffle(shuffledModules);

            int paymentsMadeForCurrentUser = 0;
            int maxModulesToBuy = Math.min(shuffledModules.size(), 5); // Student maksimum 5 ta modul sotib olsin
            int requiredPayments = 3; // Har bir student kamida 3 ta modul sotib olishi kerak

            // 3. Try to purchase modules for the current student
            for (Module module : shuffledModules) {

                // Agar student yetarlicha modul sotib olgan bo'lsa va tasodifiy shart bajarilmasa, keyingisiga o'tamiz
                // Bu hamma student hamma modulni sotib olishining oldini oladi.
                if (paymentsMadeForCurrentUser >= requiredPayments && !random.nextBoolean()) {
                    continue;
                }

                // Agar sotib olinadigan modullar soni maksimumga yetgan bo'lsa, to'xtatamiz
                if (paymentsMadeForCurrentUser >= maxModulesToBuy) {
                    break;
                }

                try {
                    // 4. Create a DTO to simulate a payment request from the frontend
                    PaymentCreateDTO paymentCreateDTO = new PaymentCreateDTO();
                    paymentCreateDTO.setModuleId(module.getId());

                    // Narxni so'mda, Double tipida beramiz (servisdagi logikaga mos)
                    double amountInSom = (double) module.getPrice() / 100;
                    paymentCreateDTO.setAmount(amountInSom);

                    paymentCreateDTO.setMaskedCardNumber(faker.finance().creditCard());
                    paymentCreateDTO.setDescription("Automatic payment simulation");

                    // 5. Use the existing PaymentService to create the payment
                    paymentService.create(paymentCreateDTO, student);

                    log.debug("  SUCCESS: Student '{}' purchased module '{}'.", student.getUsername(), module.getTitle());
                    paymentsMadeForCurrentUser++;
                    totalPaymentsCreated++;

                } catch (DataConflictException e) {
                    // Bu xato kutilgan holat: student bu modulni allaqachon sotib olgan
                    log.trace("  INFO: Student '{}' already owns module '{}'. Skipping. ({})", student.getUsername(), module.getTitle(), e.getMessage());
                } catch (Exception e) {
                    // Boshqa kutilmagan xatoliklar
                    log.error("  ERROR: Failed to create payment for student '{}' and module '{}'. Reason: {}",
                            student.getUsername(), module.getTitle(), e.getMessage());
                    totalErrors++;
                }
            }

            // Agar birorta ham modul sotib ololmagan bo'lsa, logga yozamiz
            if (paymentsMadeForCurrentUser == 0) {
                log.warn("  Could not make any new payments for student '{}'. They might own all modules already.", student.getUsername());
            }
        }

        log.info("Payment generation finished. Total new payments created: {}. Total errors encountered: {}.", totalPaymentsCreated, totalErrors);
    }

    // Bu metodni GenerateService klassingizga qo'shing

    @Transactional
    public void generateCourseReviews() {
        log.info("Starting generation of reviews for courses...");

        // 1. Get all students who have made at least one payment
        List<User> studentsWithPayments = paymentRepository.findAllDistinctUsers(); // Bu metodni repository'ga qo'shish kerak

        if (studentsWithPayments.isEmpty()) {
            log.warn("Cannot generate reviews. No students with payments found.");
            return;
        }

        log.info("Found {} students with payments. Starting review generation.", studentsWithPayments.size());
        int totalReviewsCreatedOrUpdated = 0;
        int totalErrors = 0;

        // 2. Iterate through each student
        for (User student : studentsWithPayments) {
            log.debug("Processing reviews for student: {}", student.getUsername());

            // 3. Find all courses purchased by this student (via their payments)
            List<Course> purchasedCourses = paymentRepository.findCoursesByUserId(student.getId()); // Bu metodni repository'ga qo'shish kerak

            if (purchasedCourses.isEmpty()) {
                log.trace("Student '{}' has payments but no associated courses found. Skipping.", student.getUsername());
                continue;
            }

            // 4. For each purchased course, create a review
            for (Course course : purchasedCourses) {

                // 50/50 ehtimollik bilan sharh qoldirsin, hamma kursga ham emas
                if (!random.nextBoolean()) {
                    log.trace("  Skipping review for course '{}' by chance.", course.getTitle());
                    continue;
                }

                try {
                    // 5. Create a DTO to simulate a review submission
                    ReviewCreateDTO reviewCreateDTO = new ReviewCreateDTO();
                    reviewCreateDTO.setCourseId(course.getId());

                    // Generate random rating (1 to 5)
                    reviewCreateDTO.setRating(faker.number().numberBetween(1, 6)); // 6 kirmaydi, 1-5

                    // Generate a random comment
                    reviewCreateDTO.setComment(faker.lorem().sentence(faker.number().numberBetween(5, 20)));

                    // 6. Use the existing ReviewService to create or update the review
                    reviewService.create(reviewCreateDTO, student);

                    log.debug("  SUCCESS: Review created/updated for course '{}' by student '{}'. Rating: {}",
                            course.getTitle(), student.getUsername(), reviewCreateDTO.getRating());
                    totalReviewsCreatedOrUpdated++;

                } catch (Exception e) {
                    log.error("  ERROR: Failed to create review for course '{}' by student '{}'. Reason: {}",
                            course.getTitle(), student.getUsername(), e.getMessage());
                    totalErrors++;
                }
            }
        }

        log.info("Review generation finished. Total reviews created or updated: {}. Total errors: {}.", totalReviewsCreatedOrUpdated, totalErrors);
    }

// ====================================================================
// --- YORDAMCHI METODLAR (NOYOBLIK UCHUN) ---
// ====================================================================

    private String generateUniqueUsername(Set<String> existingUsernames, String firstName, String lastName) {
        String username;
        int attempt = 0;
        do {
            // Ism va familiyadan username yasaymiz
            username = (firstName + "." + lastName).toLowerCase();
            if (attempt > 0) {
                // Agar band bo'lsa, oxiriga son qo'shamiz
                username += attempt;
            }
            attempt++;
        } while (existingUsernames.contains(username));

        existingUsernames.add(username); // Yangi username'ni to'plamga qo'shib qo'yamiz
        return username;
    }

    private String generateUniqueEmail(Set<String> existingEmails, String username) {
        String email;
        int attempt = 0;
        do {
            // Xavfsiz domen bilan email yaratamiz
            email = faker.internet().safeEmailAddress(username + (attempt > 0 ? attempt : ""));
            attempt++;
        } while (existingEmails.contains(email));

        existingEmails.add(email);
        return email;
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

    private Course createCourse(Set<String> existingTitles, Set<String> existingSlugs, User instructor, Category category) {
        Course course = new Course();
        String title = generateUniqueTitle(existingTitles, faker.job().title());
        String slug = generateUniqueSlug(slugify.slugify(title), existingSlugs);

        course.setTitle(title);
        course.setSlug(slug);
        course.setDescription(faker.lorem().paragraph(3));
        course.setInstructor(instructor);
        course.setCategory(category);
        course.setSuccess(faker.bool().bool());
        log.debug("  Creating Course: '{}'", title);
        return course;
    }

    private List<Module> generateModulesForCourse(Course course, Set<String> existingModuleTitles, Set<String> existingQuizTitles) {
        List<Module> modules = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Module module = new Module();
            String title = generateUniqueTitle(existingModuleTitles, course.getTitle() + " - Module " + i);
            Long randomPriceInSom = PREDEFINED_PRICES_IN_SOM.get(random.nextInt(PREDEFINED_PRICES_IN_SOM.size()));

            module.setTitle(title);
            module.setOrderIndex(i);
            module.setDescription(faker.lorem().sentence(12));
            module.setPrice(randomPriceInSom * 100L); // Convert price to tiyin
            module.setCourse(course);

            log.debug("    Creating Module: '{}' with price {} UZS", title, randomPriceInSom);

            module.setLessons(generateLessonsForModule(module, existingQuizTitles));
            modules.add(module);
        }
        return modules;
    }

    private List<Lesson> generateLessonsForModule(Module module, Set<String> existingQuizTitles) {
        List<Lesson> lessons = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Lesson lesson = new Lesson();
            lesson.setTitle("Lesson " + i + ": " + faker.educator().campus());
            lesson.setOrderIndex(i);
            lesson.setFree(faker.bool().bool());
            lesson.setModule(module);
            lesson.setContent(String.join("\n\n", faker.lorem().paragraphs(2)));

            log.debug("      Creating Lesson: '{}'", lesson.getTitle());

            lesson.setContents(generateContentsForLesson(lesson, existingQuizTitles));
            lessons.add(lesson);
        }
        return lessons;
    }

    private List<Content> generateContentsForLesson(Lesson lesson, Set<String> existingQuizTitles) {
        List<Content> contents = new ArrayList<>();

        log.debug("        Attaching TextContent block.");
        TextContent textContent = new TextContent();
        textContent.setBlockOrder(1);
        textContent.setText(String.join("\n\n", faker.lorem().paragraphs(5)));
        textContent.setLesson(lesson);
        contents.add(textContent);

        log.debug("        Attaching QuizContent block.");
        QuizContent quizContent = new QuizContent();
        quizContent.setBlockOrder(2);
        quizContent.setLesson(lesson);
        quizContent.setQuiz(generateQuizForContent(quizContent, existingQuizTitles));
        contents.add(quizContent);
        return contents;
    }

    private Quiz generateQuizForContent(QuizContent quizContent, Set<String> existingQuizTitles) {
        Quiz quiz = new Quiz();
        String baseTitle = "Knowledge Check: " + quizContent.getLesson().getTitle();
        String uniqueTitle = generateUniqueTitle(existingQuizTitles, baseTitle);

        log.debug("          Creating Quiz: '{}'", uniqueTitle);

        quiz.setTitle(uniqueTitle);
        quiz.setDescription(faker.lorem().sentence(20));
        quiz.setQuizContent(quizContent);
        quiz.setQuestions(generateQuestionsForQuiz(quiz));
        return quiz;
    }

    private List<Question> generateQuestionsForQuiz(Quiz quiz) {
        List<Question> questions = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Question question = new Question();
            question.setText("Question " + i + ": " + faker.lorem().sentence(10) + "?");
            question.setType(QuestionType.SINGLE_CHOICE);
            question.setQuiz(quiz);
            question.setOptions(generateOptionsForQuestion(question));
            questions.add(question);
        }
        return questions;
    }

    private List<AnswerOption> generateOptionsForQuestion(Question question) {
        List<AnswerOption> options = new ArrayList<>();
        int correctIndex = random.nextInt(4);
        for (int i = 0; i < 4; i++) {
            AnswerOption option = new AnswerOption();
            option.setText(faker.lorem().sentence(4));
            option.setCorrect(i == correctIndex);
            option.setQuestion(question);
            options.add(option);
        }
        return options;
    }

    private String generateUniqueTitle(Set<String> existingTitles, String baseTitle) {
        String title = baseTitle;
        int attempt = 1;
        while (existingTitles.contains(title)) {
            title = baseTitle + " (" + (++attempt) + ")";
        }
        existingTitles.add(title);
        return title;
    }

    private String generateUniqueSlug(String baseSlug, Set<String> existingSlugs) {
        String slug = baseSlug;
        int attempt = 1;
        while (existingSlugs.contains(slug)) {
            slug = baseSlug + "-" + (++attempt);
        }
        existingSlugs.add(slug);
        return slug;
    }

    private User getOrCreateUser(String username) {
        return userRepository.findByUsername(username).orElseGet(() -> {
            log.info("User '{}' not found. Creating a new user and profile.", username);
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword(passwordEncoder.encode("12345"));
            newUser.setRole(Role.INSTRUCTOR);
            newUser.setEnabled(true);
            UserProfile profile = new UserProfile();
            profile.setFirstName(faker.name().firstName());
            profile.setLastName(faker.name().lastName());
            profile.setEmail(faker.internet().safeEmailAddress(username));
            profile.setPhoneNumber(faker.phoneNumber().cellPhone());
            profile.setBio(faker.lorem().paragraph());
            newUser.setProfile(profile);
            profile.setUser(newUser);
            return userRepository.save(newUser);
        });
    }

    private Category getOrCreateCategory(String categoryName) {
        return categoryRepository.findByName(categoryName).orElseGet(() -> {
            log.info("Category '{}' not found. Creating a new one.", categoryName);
            Category newCategory = new Category();
            newCategory.setName(categoryName);
            newCategory.setSlug(slugify.slugify(categoryName));
            return categoryRepository.save(newCategory);
        });
    }
}