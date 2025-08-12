package uz.pdp.online_education.telegram.service.student;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import uz.pdp.online_education.enums.TransactionStatus;
import uz.pdp.online_education.model.*;
import uz.pdp.online_education.model.Module;
import uz.pdp.online_education.model.lesson.*;
import uz.pdp.online_education.repository.*;
import uz.pdp.online_education.telegram.Utils;
import uz.pdp.online_education.telegram.config.controller.OnlineEducationBot;
import uz.pdp.online_education.telegram.enums.BotMessage;
import uz.pdp.online_education.telegram.enums.UserState;
import uz.pdp.online_education.telegram.mapper.SendMsg;
import uz.pdp.online_education.telegram.model.TelegramUser;
import uz.pdp.online_education.telegram.service.message.MessageService;
import uz.pdp.online_education.telegram.service.student.template.StudentInlineKeyboardService;
import uz.pdp.online_education.telegram.service.student.template.StudentMessageService;
import uz.pdp.online_education.telegram.service.student.template.StudentReplyKeyboardService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentMessageServiceImpl implements StudentMessageService {

    // --- DEPENDENCIES ---
    private final SendMsg sendMsg;
    private final MessageService messageService;
    private final ModuleEnrollmentRepository moduleEnrollmentRepository;
    private final TelegramUserRepository telegramUserRepository;
    private final StudentInlineKeyboardService studentInlineKeyboardService;
    private final OnlineEducationBot onlineEducationBot;
    private final StudentReplyKeyboardService studentReplyKeyboardService;
    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final ContentRepository contentRepository;
    private final PaymentRepository paymentRepository;

    // --- PUBLIC HANDLER METHOD ---
    @Override
    public void handleMessage(Message message) {
        Long chatId = message.getChatId();
        String text = message.getText();
        Integer messageId = message.getMessageId();

        TelegramUser telegramUser = getOrCreateTelegramUser(chatId);
        if (telegramUser.getUser() == null) {
            onlineEducationBot.myExecute(sendMsg.sendMessage(chatId, "Please authenticate first."));
            return;
        }

        User user = telegramUser.getUser();
        UserProfile profile = user.getProfile();

        // Router for text commands and ReplyKeyboard buttons
        switch (text) {
            case Utils.START -> startMessage(chatId, message.getFrom());
            case Utils.DASHBOARD -> dashboardMessage(user, profile, chatId, messageId);
            case Utils.ReplyButtons.STUDENT_MY_COURSES -> sendMyCoursesPage(chatId, 0);
            case Utils.ReplyButtons.STUDENT_ALL_COURSES -> sendAllCoursesPage(chatId, 0);
            case Utils.ReplyButtons.STUDENT_BALANCE -> sendBalanceMenu(chatId);
            case Utils.ReplyButtons.STUDENT_HELP -> askForSupportMessage(chatId);
        }
    }

    private void startMessage(Long chatId, org.telegram.telegrambots.meta.api.objects.User from) {
        telegramUserRepository.updateStateByChatId(chatId, UserState.STUDENT_MAIN_MENU);
        ReplyKeyboardMarkup replyKeyboardMarkup = studentReplyKeyboardService.studentMainMenu();
        SendMessage sendMessage = sendMsg.sendMessage(
                chatId,
                messageService.getMessage(BotMessage.START_MESSAGE_STUDENT, from.getUserName()),
                replyKeyboardMarkup);
        onlineEducationBot.myExecute(sendMessage);
    }

    private void dashboardMessage(User user, UserProfile profile, Long chatId, Integer messageId) {
        // Dashboard is a one-time view, so no state change is needed.
        String dashboardText = prepareStudentDashboardText(user, profile);
        InlineKeyboardMarkup inlineKeyboardMarkup = studentInlineKeyboardService.dashboardMenu();
        SendMessage sendMessage = sendMsg.sendMessage(chatId, dashboardText, inlineKeyboardMarkup);
        onlineEducationBot.myExecute(sendMessage);
        onlineEducationBot.myExecute(sendMsg.editMarkup(chatId, messageId));
    }

    private void sendMyCoursesPage(Long chatId, int pageNumber) {
        telegramUserRepository.updateStateByChatId(chatId, UserState.STUDENT_VIEWING_MY_COURSES);


        Message message = onlineEducationBot.MyExecute(sendMsg.sendMessage(chatId, "Siz tanlagan kurslar", new ReplyKeyboardRemove(true)));
        onlineEducationBot.myExecute(sendMsg.deleteMessage(chatId, message.getMessageId()));

        // This method can use the "send and edit" pattern for better UX.
        // For now, keeping the original logic as requested.
        User user = telegramUserRepository.findByChatId(chatId).get().getUser();
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Course> coursesPage = moduleEnrollmentRepository.findEnrolledCoursesByUserId(user.getId(), pageable);

        if (!coursesPage.hasContent()) {
            String noCoursesText = messageService.getMessage(BotMessage.STUDENT_MY_COURSES_NO_COURSES);
            onlineEducationBot.myExecute(sendMsg.sendMessage(chatId, noCoursesText));
            return;
        }

        String text = buildMyCoursesText(user.getId(), coursesPage);
        InlineKeyboardMarkup keyboard = studentInlineKeyboardService.myCoursesListPage(coursesPage);
        SendMessage sendMessage = sendMsg.sendMessage(chatId, text, keyboard);
        onlineEducationBot.myExecute(sendMessage);
    }

    private void sendAllCoursesPage(Long chatId, int pageNumber) {
        telegramUserRepository.updateStateByChatId(chatId, UserState.STUDENT_VIEWING_ALL_COURSES);
        // TODO: Implement logic for showing all courses
        onlineEducationBot.myExecute(sendMsg.sendMessage(chatId, "'Barcha Kurslar' bo'limi ishlab chiqilmoqda."));
    }

    private void sendBalanceMenu(Long chatId) {
        telegramUserRepository.updateStateByChatId(chatId, UserState.STUDENT_MANAGING_BALANCE);
        // TODO: Implement logic for showing the balance menu
        onlineEducationBot.myExecute(sendMsg.sendMessage(chatId, "'Balans va To'lovlar' bo'limi ishlab chiqilmoqda."));
    }

    private void askForSupportMessage(Long chatId) {
        telegramUserRepository.updateStateByChatId(chatId, UserState.STUDENT_AWAITING_SUPPORT_MESSAGE);
        // TODO: Implement logic for asking for a support message
        onlineEducationBot.myExecute(sendMsg.sendMessage(chatId, "'Yordam' bo'limi ishlab chiqilmoqda."));
    }

    // sendWelcomeMessage metodi endi public bo'lishi kerak,
    // chunki uni Callback servisi ham chaqiradi.
    @Override
    public void sendWelcomeMessage(Long chatId, String username) {
        // ... avvalgi kod ...
    }

    // --- PRIVATE IMPLEMENTATION METHODS ---

    /**
     * Edits an existing message to show a specific page of the user's enrolled courses.
     * Used for pagination.
     *
     * @param chatId     The student's chat ID.
     * @param messageId  The ID of the message to be edited.
     * @param pageNumber The new page number to display.
     */
    @Override
    public void editMyCoursesPage(Long chatId, Integer messageId, int pageNumber) {
        // sendMyCoursesPage metodidagi logikani deyarli takrorlaydi,
        // faqat oxirida 'SendMessage' o'rniga 'EditMessageText' yottaradian.
        User user = telegramUserRepository.findByChatId(chatId).get().getUser();
//        int pageSize = 5;
//        Pageable pageable = PageRequest.of(pageNumber, pageSize);
//        Page<CourseWithProgressDTO> coursesPage = moduleEnrollmentRepository.findEnrolledCoursesWithProgress(user.getId(), pageable);

        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Course> coursesPage = moduleEnrollmentRepository.findEnrolledCoursesByUserId(user.getId(), pageable);

        if (!coursesPage.hasContent()) {
            // Bu holatda tahrirlash o'rniga "Kurslar yo'q" deb javob berish mumkin
            // yoki hech narsa qilmaslik.
            return;
        }

        String finalText = buildMyCoursesText(user.getId(), coursesPage); // Avval yaratgan yordamchi metodimiz
        InlineKeyboardMarkup keyboard = studentInlineKeyboardService.myCoursesListPage(coursesPage);

        // Xabarni tahrirlash
        EditMessageText editMessage = sendMsg.editMessage(chatId, messageId, finalText, keyboard);
        onlineEducationBot.myExecute(editMessage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendCourseModulesPage(Long chatId, Integer messageId, Long courseId) {
        User user = telegramUserRepository.findByChatId(chatId)
                .orElseThrow(() -> new RuntimeException("TelegramUser not found"))
                .getUser();

        // 1. Kurs mavjudligini va nomini tekshirish
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course with id " + courseId + " not found"));

        // 2. Foydalanuvchining shu kursdagi barcha a'zoliklarini (modullarini) olish
        List<ModuleEnrollment> enrollments = moduleEnrollmentRepository.findEnrollmentsByUserAndCourse(user.getId(), courseId);

        if (enrollments.isEmpty()) {
            // Agar modul topilmasa, foydalanuvchiga bu haqida xabar beramiz
            String noModulesText = messageService.getMessage(BotMessage.STUDENT_COURSE_MODULES_NO_MODULES);
            InlineKeyboardMarkup backKeyboard = studentInlineKeyboardService.backToMyCourses(); // Orqaga tugmasi
            EditMessageText editMessage = sendMsg.editMessage(chatId, messageId, noModulesText, backKeyboard);
            onlineEducationBot.myExecute(editMessage);
            return;
        }

        // 3. Xabar matnini yig'ish
        String text = buildCourseModulesText(course.getTitle(), enrollments);

        // 4. Klaviatura yasash
        InlineKeyboardMarkup keyboard = studentInlineKeyboardService.courseModulesPage(courseId, enrollments);

        // 5. Avvalgi xabarni tahrirlash
        EditMessageText editMessage = sendMsg.editMessage(chatId, messageId, text, keyboard);
        onlineEducationBot.myExecute(editMessage);
    }

    /**
     * Helper method to build the text for the course modules list.
     */
    private String buildCourseModulesText(String courseTitle, List<ModuleEnrollment> enrollments) {
        StringBuilder textBuilder = new StringBuilder();

        // Asosiy sarlavhani .yml dan olish
        textBuilder.append(messageService.getMessage(
                BotMessage.STUDENT_COURSE_MODULES_HEADER,
                courseTitle
        ));
        textBuilder.append("\n\n");

        // Har bir modul uchun ro'yxat elementini .yml dan olib formatlash
        for (int i = 0; i < enrollments.size(); i++) {
            ModuleEnrollment enrollment = enrollments.get(i);
            Module module = enrollment.getModule();
            int progress = (int) enrollment.getProgressPercentage();
            int moduleNumber = i + 1; // Yoki modulning o'zining orderIndex'i

            textBuilder.append(messageService.getMessage(
                    BotMessage.STUDENT_COURSE_MODULES_LIST_ITEM,
                    moduleNumber,
                    module.getTitle(),
                    progress,
                    createProgressBar(progress)
            ));
            textBuilder.append("\n");
        }
        return textBuilder.toString();
    }

    @Override
    @Transactional(readOnly = true)
    public void sendLessonListPage(Long chatId, Integer messageId, Long moduleId) {
        User user = telegramUserRepository.findByChatId(chatId).get().getUser();
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found"));

        // --- ASOSIY MANTIQ SHU YERDA ---
        // 1. Foydalanuvchi bu modul uchun TO'LOV QILGANMI?
        boolean hasPaid = paymentRepository.existsByUserIdAndModuleIdAndStatus(
                user.getId(),
                moduleId,
                TransactionStatus.SUCCESS // Yoki sizdagi "muvaffaqiyatli" status
        );

        // 2. Modulga tegishli barcha darslarni olish
        List<Lesson> lessons = lessonRepository.findAllByModuleIdOrderByOrderIndexAsc(moduleId);

        // 3. Xabar matnini va klaviaturani foydalanuvchining to'lov holatiga qarab yasash
        String text = buildLessonListText(module, lessons, hasPaid);
        InlineKeyboardMarkup keyboard = studentInlineKeyboardService.lessonListPage(module, lessons, hasPaid);

        // 4. Avvalgi xabarni tahrirlash
        EditMessageText editMessage = sendMsg.editMessage(chatId, messageId, text, keyboard);
        onlineEducationBot.myExecute(editMessage);
    }

    // --- Yordamchi Metodlar ---

    private void showLessonsForEnrolledUser(Long chatId, Integer messageId, Module module) {
        List<Lesson> lessons = lessonRepository.findAllByModuleIdOrderByOrderIndexAsc(module.getId());
        String text = buildLessonListText(module, lessons, true); // isEnrolled = true
        InlineKeyboardMarkup keyboard = studentInlineKeyboardService.lessonListPage(module, lessons, true);
        onlineEducationBot.myExecute(sendMsg.editMessage(chatId, messageId, text, keyboard));
    }

    private void showLessonsForUnenrolledUser(Long chatId, Integer messageId, Module module) {
        List<Lesson> lessons = lessonRepository.findAllByModuleIdOrderByOrderIndexAsc(module.getId());
        String text = buildLessonListText(module, lessons, false); // isEnrolled = false
        InlineKeyboardMarkup keyboard = studentInlineKeyboardService.lessonListPage(module, lessons, false);
        onlineEducationBot.myExecute(sendMsg.editMessage(chatId, messageId, text, keyboard));
    }

    private void showFullyPaidModuleMessage(Long chatId, Integer messageId, Module module) {
        String text = messageService.getMessage(BotMessage.STUDENT_LESSON_FULLY_PAID_MODULE, module.getTitle());

        // Bu yerda klaviatura faqat "Sotib olish" va "Orqaga" tugmalaridan iborat bo'ladi
        InlineKeyboardMarkup keyboard = studentInlineKeyboardService.buyOnlyKeyboard(module); // Bu metodni yaratish kerak

        onlineEducationBot.myExecute(sendMsg.editMessage(chatId, messageId, text, keyboard));
    }

    private String buildLessonListText(Module module, List<Lesson> lessons, boolean isEnrolled) {
        StringBuilder textBuilder = new StringBuilder();
        textBuilder.append(String.format("📖 <b>Modul: %s</b>\n", module.getTitle()));

        if (!isEnrolled) {
            textBuilder.append(String.format("_Ushbu modul pullik. Narxi: <b>%d UZS</b>_\n", module.getPrice()));
        }
        textBuilder.append("_Ko'rish uchun darsni tanlang:_\n\n");

        for (Lesson lesson : lessons) {
            String statusIcon = lesson.isFree() ? "🆓" : (isEnrolled ? "✅" : "🔒");
            textBuilder.append(String.format("%d. %s (%s)\n",
                    lesson.getOrderIndex(),
                    lesson.getTitle(),
                    statusIcon
            ));
        }
        return textBuilder.toString();
    }

//    @Override
//    @Transactional(readOnly = true)
//    public void sendLessonContent(Long chatId, Integer messageId, Long lessonId) {
//        // Step 1: Fetch the lesson and its module
//        Lesson lesson = lessonRepository.findById(lessonId)
//                .orElseThrow(() -> new RuntimeException("Lesson not found"));
//        Module module = lesson.getModule();
//
//        // Step 2: Delete the previous menu message to keep the chat clean
//        onlineEducationBot.myExecute(sendMsg.deleteMessage(chatId, messageId));
//
//        // Step 3: Find and send all AttachmentContent (videos, images, files)
//        List<Content> contents = lesson.getContents(); // Assuming lazy loading works due to @Transactional
//        for (Content content : contents) {
//            if (content instanceof AttachmentContent attachmentContent) {
//                // Assuming you have a file_id stored from a previous upload
//                String fileId = attachmentContent.getAttachment().getBucketName();
//                SendVideo sendVideo = new SendVideo(chatId.toString(), new InputFile(fileId));
//                sendVideo.setCaption(lesson.getTitle()); // Add lesson title as a caption

    /// /                onlineEducationBot.myExecute(sendVideo);
//            }
//        }
//
//        // Step 4: Prepare and send the text content message
//        String textContent = buildLessonTextContent(lesson, contents);
//        String lessonUrlOnSite = "http://your-site.com/courses/" + module.getCourse().getId() + "/modules/" + module.getId() + "/lessons/" + lesson.getId();
//        InlineKeyboardMarkup keyboard = studentInlineKeyboardService.lessonViewKeyboard(module.getId(), lessonUrlOnSite);
//
//        SendMessage textMessage = sendMsg.sendMessage(chatId, textContent, keyboard);
//        onlineEducationBot.myExecute(textMessage);
//    }
    @Override
    @Transactional(readOnly = true) // Lazy-loading uchun
    public void sendLessonMainMenu(Long chatId, Integer messageId, Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        String text = messageService.getMessage(
                BotMessage.STUDENT_LESSON_MAIN_MENU,
                lesson.getTitle(),
                lesson.getContent() // Lesson'dagi qisqacha tavsif
        );

        InlineKeyboardMarkup keyboard = studentInlineKeyboardService.lessonContentMenu(lesson);

        EditMessageText editMessage = sendMsg.editMessage(chatId, messageId, text, keyboard);
        onlineEducationBot.myExecute(editMessage);
    }

    @Override
    @Transactional(readOnly = true)
    public void sendContent(Long chatId, Long contentId) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Content not found"));

        if (content instanceof AttachmentContent attachmentContent) {
            String fileId = attachmentContent.getAttachment().getBucketName();
            SendVideo sendVideo = new SendVideo(chatId.toString(), new InputFile(fileId));
            // Videoga izoh qo'shish ixtiyoriy
            sendVideo.setCaption(content.getLesson().getTitle());
//            onlineEducationBot.myExecute(sendVideo);

        } else if (content instanceof TextContent textContent) {
            // TODO: Telegramning 4096 belgilik limitini hisobga olib, matnni
            // qismlarga bo'lib yuboradigan logikani qo'shish kerak.
            onlineEducationBot.myExecute(sendMsg.sendMessage(chatId, textContent.getText()));

        } else if (content instanceof QuizContent quizContent) {
            String text = messageService.getMessage(BotMessage.STUDENT_LESSON_QUIZ_TEXT);
            InlineKeyboardMarkup keyboard = studentInlineKeyboardService.quizButton(quizContent.getId());
            onlineEducationBot.myExecute(sendMsg.sendMessage(chatId, text, keyboard));
        }
    }

    private String buildLessonTextContent(Lesson lesson, List<Content> contents) {
        StringBuilder textBuilder = new StringBuilder();
        // Add the header from the .yml file
        textBuilder.append(messageService.getMessage(BotMessage.STUDENT_LESSON_CONTENT_HEADER, lesson.getTitle()));

        // Append all TextContent blocks
        for (Content content : contents) {
            if (content instanceof TextContent textContentBlock) {
                textBuilder.append(textContentBlock.getText());
                textBuilder.append("\n\n");
            }
        }
        return textBuilder.toString();
    }

    // --- PRIVATE HELPER METHODS ---


    private String buildMyCoursesText(Long userId, Page<Course> coursesPage) {
        StringBuilder textBuilder = new StringBuilder();
        textBuilder.append(String.format(
                messageService.getMessage(BotMessage.STUDENT_MY_COURSES_HEADER),
                coursesPage.getNumber() + 1,
                coursesPage.getTotalPages()
        )).append("\n\n");

        List<Course> courses = coursesPage.getContent();
        for (int i = 0; i < courses.size(); i++) {
            Course course = courses.get(i);
            int courseNumber = coursesPage.getNumber() * coursesPage.getSize() + i + 1;
            int progress = calculateCourseProgress(userId, course.getId());
            String progressBar = createProgressBar(progress);
            textBuilder.append(String.format(
                    messageService.getMessage(BotMessage.STUDENT_MY_COURSES_LIST_ITEM),
                    courseNumber, course.getTitle(), progress, progressBar
            )).append("\n");
        }
        return textBuilder.toString();
    }

    private int calculateCourseProgress(Long userId, Long courseId) {
        Double averageProgress = moduleEnrollmentRepository.findAverageProgressForCourse(userId, courseId);
        return (averageProgress != null) ? averageProgress.intValue() : 0;
    }

    private String prepareStudentDashboardText(User user, UserProfile profile) {
        Integer activeCoursesCount = moduleEnrollmentRepository.countActiveCoursesByUserId(user.getId());
        Double averageProgressDouble = moduleEnrollmentRepository.findAverageProgressByUserId(user.getId());
        int averageProgress = (averageProgressDouble != null) ? averageProgressDouble.intValue() : 0;
        Integer completedModulesCount = moduleEnrollmentRepository.countCompletedModulesByUserId(user.getId());
        String progressBar = createProgressBar(averageProgress);

        return messageService.getMessage(
                BotMessage.DASHBOARD_STUDENT,
                profile.getFirstName() + " " + profile.getLastName(),
                profile.getEmail(), user.getUsername(), user.getRole().name(),
                activeCoursesCount, averageProgress, progressBar, completedModulesCount
        );
    }

    private String createProgressBar(int percentage) {
        if (percentage < 0) percentage = 0;
        if (percentage > 100) percentage = 100;
        int filledBlocks = Math.round(percentage / 10.0f);
        int emptyBlocks = 10 - filledBlocks;
        return "█".repeat(filledBlocks) + "░".repeat(emptyBlocks);
    }

    private TelegramUser getOrCreateTelegramUser(Long chatId) {
        return telegramUserRepository.findByChatId(chatId).orElseGet(() -> {
            log.info("Creating a new TelegramUser for chatId: {}", chatId);
            TelegramUser newTelegramUser = new TelegramUser();
            newTelegramUser.setChatId(chatId);
            newTelegramUser.setUserState(UserState.UNREGISTERED);
            return telegramUserRepository.save(newTelegramUser);
        });
    }
}