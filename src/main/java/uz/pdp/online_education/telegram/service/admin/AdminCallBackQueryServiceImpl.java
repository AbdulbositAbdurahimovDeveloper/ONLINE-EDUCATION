package uz.pdp.online_education.telegram.service.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import uz.pdp.online_education.enums.Role;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.mapper.CourseMapper;
import uz.pdp.online_education.model.Category;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.model.UserProfile;
import uz.pdp.online_education.payload.course.CourseInfoDTO;
import uz.pdp.online_education.repository.CategoryRepository;
import uz.pdp.online_education.repository.CourseRepository;
import uz.pdp.online_education.repository.TelegramUserRepository;
import uz.pdp.online_education.repository.UserRepository;
import uz.pdp.online_education.telegram.Utils;
import uz.pdp.online_education.telegram.config.controller.OnlineEducationBot;
import uz.pdp.online_education.telegram.enums.BotMessage;
import uz.pdp.online_education.telegram.enums.UserState;
import uz.pdp.online_education.telegram.mapper.SendMsg;
import uz.pdp.online_education.telegram.service.TelegramUserService;
import uz.pdp.online_education.telegram.service.admin.template.AdminCallBackQueryService;
import uz.pdp.online_education.telegram.service.admin.template.AdminMessageService;
import uz.pdp.online_education.telegram.service.admin.template.InlineKeyboardService;
import uz.pdp.online_education.telegram.service.message.MessageService;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCallBackQueryServiceImpl implements AdminCallBackQueryService {

    private final TelegramUserRepository telegramUserRepository;
    private final UserRepository userRepository;
    private final InlineKeyboardService inlineKeyboardService;
    private final MessageService messageService;
    private final OnlineEducationBot onlineEducationBot;
    private final SendMsg sendMsg;
    private final AdminMessageService adminMessageService;
    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;
    private final TelegramUserService telegramUserService;
    private final CategoryRepository categoryRepository;
    private final ObjectMapper objectMapper;
    private final AdminMessageServiceImpl adminMessageServiceImpl;


    @Override
    public void handleCallback(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();

        User user = telegramUserRepository.findByChatId(chatId).orElseThrow().getUser();

        String[] params = data.split(":");
        if (!user.getRole().equals(Role.ADMIN)) {
            return;
        }
//        if (!params[0].equals("admin") || !params[0].equals("auth")) return;

        String context = params[1];

        switch (context) {
            case Utils.CallbackData.ACTION_LOGOUT -> handleAuthCallback(user, chatId, messageId, params);
            case "users" -> handleUserCallbacks(chatId, messageId, data);
            case "courses" -> handleCourseCallbacks(chatId, messageId, data);
            case "broadcast" -> handleBroadcastCallbacks(callbackQuery, params);
            case "main_menu" -> {
                onlineEducationBot.myExecute(sendMsg.deleteMessage(chatId, messageId));
                adminMessageService.sendAdminWelcomeMessage(chatId, getProfile(chatId));
            }
        }
    }



    private void handleAuthCallback(User user, Long chatId, Integer messageId, String[] data) {
        String action = data[1]; // "logout"
        String step = data[2];  // "init", "confirm", "cancel"

        if (!action.equals("logout")) {
            log.warn("Noma'lum autentifikatsiya amali: {}", action);
            return;
        }

        switch (step) {
            case Utils.CallbackData.ACTION_INIT -> {
                String confirmationText = messageService.getMessage(BotMessage.AUTH_LOGOUT_CONFIRMATION_TEXT);
                InlineKeyboardMarkup confirmationKeyboard = inlineKeyboardService.logoutConfirmation();
                onlineEducationBot.myExecute(sendMsg.editMessage(chatId, messageId, confirmationText, confirmationKeyboard));
            }
            case Utils.CallbackData.ACTION_CONFIRM -> {
                telegramUserService.unregistered(chatId);
                String successText = messageService.getMessage(BotMessage.AUTH_LOGOUT_SUCCESS_TEXT);
                onlineEducationBot.myExecute(sendMsg.editMessage(chatId, messageId, successText, null));
            }
            case Utils.CallbackData.ACTION_CANCEL -> adminMessageService.showDashboard(user, chatId, messageId);
            default -> log.warn("Noma'lum chiqish qadami: {}", step);
        }
    }

    private void handleUserCallbacks(Long chatId, Integer messageId, String data) {
        String[] params = data.split(":");
        String action = params[2];

        switch (action) {
            case "page" -> sendUsersListPage(chatId, messageId, Integer.parseInt(params[3]), null);
            case "search_page" -> sendUsersListPage(chatId, messageId, Integer.parseInt(params[4]), params[3]);
            case "view" -> {

                String backCallback = "admin:users:page:0";
                sendUserDetail(chatId, messageId, Long.parseLong(params[3]), backCallback);
            }
            case "main_menu" -> sendUsersMainMenu(chatId, messageId);
            case "search_init" -> initiateUserSearch(chatId, messageId);
            case "stats" -> showUserStats(chatId, messageId);
        }
    }


    private void sendUsersListPage(Long chatId, Integer messageId, int pageNumber, String searchTerm) {
        Pageable pageable = PageRequest.of(pageNumber, 10, Sort.by("id"));
        Page<User> userPage;
        StringBuilder text = new StringBuilder();

        if (searchTerm == null || searchTerm.isBlank()) {
            userPage = userRepository.findAll(pageable);
            text.append(String.format("👥 *Foydalanuvchilar ro'yxati*\n_Sahifa: %d / %d_\n\n", pageNumber + 1, userPage.getTotalPages()));
        } else {
            userPage = userRepository.searchUsers(searchTerm, pageable);
            text.append(String.format("🔍 *'%s' bo'yicha topilgan natijalar*\n_Sahifa: %d / %d_\n\n", searchTerm, pageNumber + 1, userPage.getTotalPages()));
        }

        List<User> usersOnPage = userPage.getContent();
        if (usersOnPage.isEmpty()) {
            text.append("Foydalanuvchilar topilmadi.");
        } else {
            for (int i = 0; i < usersOnPage.size(); i++) {
                User user = usersOnPage.get(i);
                UserProfile profile = user.getProfile();
                String name = (profile != null && profile.getFirstName() != null && !profile.getFirstName().isBlank()) ? profile.getFirstName() + " " + profile.getLastName() : user.getUsername();
                text.append(String.format("`%d.` 👤 **%s** — `%s`\n", i + 1, escapeMarkdown(name), user.getRole().name()));
            }
            text.append("\n🔽 Tanlash uchun tegishli tugmani bosing.");
        }


        InlineKeyboardMarkup keyboard = inlineKeyboardService.usersPageMenu(userPage, searchTerm);

        EditMessageText editMessage = sendMsg.editMessage(chatId, messageId, text.toString(), keyboard);
        editMessage.setParseMode("Markdown");
        onlineEducationBot.myExecute(editMessage);
    }

    private void sendUserDetail(Long chatId, Integer messageId, Long userId, String backCallback) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
        UserProfile profile = user.getProfile();

        String text = String.format(
                "👤 *Foydalanuvchi Profili*\n\n" +
                        "*ID:* `%d`\n" +
                        "*Ism-Familiya:* %s\n" +
                        "*Username:* @%s\n" +
                        "*Email:* `%s`\n" +
                        "*Rol:* `%s`\n" +
                        "*Ro'yxatdan o'tgan:* %s",
                user.getId(),
                escapeMarkdown(profile.getFirstName() + " " + profile.getLastName()),
                escapeMarkdown(user.getUsername()),
                escapeMarkdown(profile.getEmail()),
                user.getRole(),
                user.getCreatedAt().toString().formatted((DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm")))
        );

        InlineKeyboardMarkup keyboard = inlineKeyboardService.userDetailMenu(userId, backCallback);
        EditMessageText editMessage = sendMsg.editMessage(chatId, messageId, text, keyboard);
        editMessage.setParseMode("Markdown");
        onlineEducationBot.myExecute(editMessage);
    }

    private void sendUsersMainMenu(Long chatId, Integer messageId) {
        String menuText = messageService.getMessage(BotMessage.ADMIN_USERS_MENU);
        InlineKeyboardMarkup keyboard = inlineKeyboardService.usersMainMenu();
        onlineEducationBot.myExecute(sendMsg.editMessage(chatId, messageId, menuText, keyboard));
    }

    private void initiateUserSearch(Long chatId, Integer messageId) {
        telegramUserRepository.updateStateByChatId(chatId, UserState.ADMIN_AWAITING_USER_SEARCH_QUERY);
        String text = "Iltimos, qidirish uchun foydalanuvchining ismini, usernamesini yoki emailini kiriting.";
        InlineKeyboardMarkup keyboard = inlineKeyboardService.createSingleButtonKeyboard("⬅️ Orqaga", "admin:users:main_menu");
        onlineEducationBot.myExecute(sendMsg.editMessage(chatId, messageId, text, keyboard));
    }

    private void showUserStats(Long chatId, Integer messageId) {
        long totalUsers = userRepository.count();
        String text = "Foydalanuvchilar statistikasi:\n\nJami: " + totalUsers + " ta.";
        InlineKeyboardMarkup keyboard = inlineKeyboardService.createSingleButtonKeyboard("⬅️ Orqaga", "admin:users:main_menu");
        onlineEducationBot.myExecute(sendMsg.editMessage(chatId, messageId, text, keyboard));
    }

    private UserProfile getProfile(Long chatId) {
        return telegramUserRepository.findByChatId(chatId)
                .orElseThrow(() -> new EntityNotFoundException("Telegram User not found"))
                .getUser().getProfile();
    }


    private String escapeMarkdown(String text) {
        if (text == null) return "";
        return text.replace("_", "\\_").replace("*", "\\*").replace("`", "\\`")
                .replace("[", "\\[").replace("]", "\\]");
    }


    private void handleCourseCallbacks(Long chatId, Integer messageId, String data) {
        String[] params = data.split(":");
        String action = params[2];

        switch (action) {
            case "main_menu" -> sendCoursesMainMenu(chatId, messageId);
            case "browse" -> handleCourseBrowseView(chatId, messageId, params);
            case "search_init" -> initiateCourseSearch(chatId, messageId);
            case "stats" -> showCourseStats(chatId, messageId);
            case "by_category_courses" -> { // <-- YANGI QISM
                Long categoryId = Long.parseLong(params[3]);
                int page = Integer.parseInt(params[5]);
                sendCoursesByCategoryIdPage(chatId, messageId, categoryId, page);
            }
            case "by_category" -> { // <-- YANGI QISM
                int page = Integer.parseInt(params[4]);
                sendCategoriesListPage(chatId, messageId, page);
            }
            case "mentor_courses" -> {
                Long mentorId = Long.parseLong(params[3]);
                int page = Integer.parseInt(params[5]);
                sendCoursesListPage(chatId, messageId, page, null, mentorId, null);
            }


            case "page" -> sendCoursesListPage(chatId, messageId, Integer.parseInt(params[3]), null, null, null);
            case "search_page" ->
                    sendCoursesListPage(chatId, messageId, Integer.parseInt(params[4]), params[3], null, null);
            case "list_by_mentor" -> sendMentorsListPage(chatId, messageId, Integer.parseInt(params[4]));
            case "list_by_mentor_courses" ->
                    sendCoursesByMentorPage(chatId, messageId, Long.parseLong(params[3]), Integer.parseInt(params[5]));


            case "view" -> {
                if (params.length < 5) return;
                Long courseId = Long.parseLong(params[3]);
                String fromContext = params[4];
                String backCallback;


                if (fromContext.equals("search")) {
                    if (params.length < 7) return;
                    String searchTerm = params[5];
                    int page = Integer.parseInt(params[6]);
                    backCallback = "admin:courses:search_page:" + searchTerm + ":" + page;
                } else if (fromContext.equals("mentor")) {
                    if (params.length < 7) return;
                    Long mentorId = Long.parseLong(params[5]);
                    int page = Integer.parseInt(params[6]);
                    backCallback = "admin:courses:mentor_courses:" + mentorId + ":page:" + page;
                } else if (fromContext.equals("category")) {

                    if (params.length < 7) return;
                    Long categoryId = Long.parseLong(params[5]);
                    int page = Integer.parseInt(params[6]);

                    backCallback = "admin:courses:by_category_courses:" + categoryId + ":page:" + page;
                } else {

                    if (params.length < 6) return;
                    int page = Integer.parseInt(params[5]);
                    backCallback = "admin:courses:page:" + page;
                }

                sendCourseDetail(chatId, messageId, courseId, backCallback);
                break;
            }
            case "list" -> handleCourseListView(chatId, messageId, params);
        }
    }



//    private void handleCourseCallbacks(Long chatId, Integer messageId, String data) {
//        String[] params = data.split(":");
//        if (params.length < 3) return;
//        String action = params[2];
//
//        switch (action) {
//            case "page":
//
//                sendCoursesListPage(chatId, messageId, Integer.parseInt(params[3]), null, null);
//                break;
//            case "search_page":
//
//                sendCoursesListPage(chatId, messageId, Integer.parseInt(params[4]), params[3], null);
//                break;
//            case "list_by_mentor_courses":
//
//                sendCoursesListPage(chatId, messageId, Integer.parseInt(params[5]), null, Long.parseLong(params[3]));
//                break;
//
//
//            case "view":
//                Long courseId = Long.parseLong(params[3]);
//                String backCallback;
//
//
//                if (params.length > 5 && params[4].equals("search")) {
//
//                    String searchTerm = params[5];
//                    int page = Integer.parseInt(params[6]);
//                    backCallback = "admin:courses:search_page:" + searchTerm + ":" + page;
//                } else if (params.length > 5 && params[4].equals("mentor")) {
//
//                    Long mentorId = Long.parseLong(params[5]);
//                    int page = Integer.parseInt(params[6]);
//                    backCallback = "admin:courses:list_by_mentor_courses:" + mentorId + ":page:" + page;
//                } else {
//
//                    int page = Integer.parseInt(params[5]);
//                    backCallback = "admin:courses:page:" + page;
//                }
//
//                sendCourseDetail(chatId, messageId, courseId, backCallback);
//                break;
//
//            case "main_menu":
//                sendCoursesMainMenu(chatId, messageId);
//                break;
//            case "search_init":
//                initiateCourseSearch(chatId, messageId);
//                break;
//            case "stats":
//                showCourseStats(chatId, messageId);
//                break;
//        }
//    }

    private void sendCourseBrowseMenu(Long chatId, Integer messageId) {
        String menuText = "Kurslarni qanday usulda ko'rmoqchisiz?";
        InlineKeyboardMarkup keyboard = inlineKeyboardService.courseBrowseMethodMenu("admin:courses:main_menu");
        onlineEducationBot.myExecute(sendMsg.editMessage(chatId, messageId, menuText, keyboard));
    }

    private void handleCourseBrowseView(Long chatId, Integer messageId, String[] params) {
        if (params.length < 4) return;
        String listType = params[3];

        switch (listType) {
            case "init" -> {
                String menuText = "Kurslarni qanday usulda ko'rmoqchisiz?";
                InlineKeyboardMarkup keyboard = inlineKeyboardService.courseBrowseMethodMenu("admin:courses:main_menu");
                onlineEducationBot.myExecute(sendMsg.editMessage(chatId, messageId, menuText, keyboard));
            }
            case "by_mentor" -> {
                int page = Integer.parseInt(params[5]);
                sendMentorsListPage(chatId, messageId, page);
            }
            case "by_mentor_courses" -> {
                Long mentorId = Long.parseLong(params[4]);
                int page = Integer.parseInt(params[6]);
                sendCoursesListPage(chatId, messageId, page, null, mentorId, null);
            }


        }
    }


    @Transactional(readOnly = true)
    protected void sendCoursesListPage(Long chatId, Integer messageId, int pageNumber, String searchTerm, Long mentorId, Long categoryId) {
        Pageable pageable = PageRequest.of(pageNumber, 10, Sort.by("id"));
        Page<Course> coursePage;
        StringBuilder text = new StringBuilder();

        if (searchTerm != null && !searchTerm.isBlank()) {
            coursePage = courseRepository.searchByTitle(searchTerm, pageable);
            text.append(String.format("🔍 *'%s'* bo'yicha topilgan natijalar\n", escapeMarkdown(searchTerm)));
        } else if (mentorId != null) {
            coursePage = courseRepository.findAllByInstructorIdAndDeletedFalse(mentorId, pageable);
            User mentor = userRepository.findById(mentorId).orElse(null);
            String mentorName = (mentor != null && mentor.getProfile() != null) ? mentor.getProfile().getFirstName() + " " + mentor.getProfile().getLastName() : "";
            text.append(String.format("📚 *%s* mentorining kurslari\n", escapeMarkdown(mentorName)));
        } else if (categoryId != null) {
            coursePage = courseRepository.findAllByCategoryIdAndDeletedFalse(categoryId, pageable);
            Category category = categoryRepository.findById(categoryId).orElse(null);
            String categoryName = (category != null) ? category.getName() : "";
            text.append(String.format("🗂 *'%s'* kategoriyasidagi kurslar\n", escapeMarkdown(categoryName)));
        } else {
            coursePage = courseRepository.findAll(pageable);
            text.append("📖 *Barcha kurslar ro'yxati*\n");
        }

        text.append(String.format("_Sahifa: %d / %d_\n\n", pageNumber + 1, coursePage.getTotalPages()));
        List<Course> coursesOnPage = coursePage.getContent();
        if (coursesOnPage.isEmpty()) {
            text.append("Kurslar topilmadi.");
        } else {
            for (int i = 0; i < coursesOnPage.size(); i++) {
                Course course = coursesOnPage.get(i);
                String statusEmoji = course.isDeleted() ? "❌" : "✅";
                String courseCategoryName = (course.getCategory() != null) ? course.getCategory().getName() : "Noma'lum";
                text.append(String.format("`%d.` %s **%s** — _%s_\n", i + 1, statusEmoji, escapeMarkdown(course.getTitle()), escapeMarkdown(courseCategoryName)));
            }
            text.append("\n🔽 Tanlash uchun tegishli tugmani bosing.");
        }

        InlineKeyboardMarkup keyboard = inlineKeyboardService.coursesPageMenu(coursePage, searchTerm, mentorId, categoryId);
        EditMessageText editMessage = sendMsg.editMessage(chatId, messageId, text.toString(), keyboard);
        editMessage.setParseMode("Markdown");
        onlineEducationBot.myExecute(editMessage);
    }


    @Transactional(readOnly = true)
    protected void sendCourseDetail(Long chatId, Integer messageId, Long courseId, String originalCallback) {
        Course course = courseRepository.findByIdWithModules(courseId).orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + courseId));
        CourseInfoDTO dto = courseMapper.toInfoDto(course);

        String[] params = originalCallback.split(":");
        int pageNumber = Integer.parseInt(params[params.length - 1]);
        String backCallback;
        if (originalCallback.contains(":mentor_courses:")) {
            backCallback = "admin:courses:mentor_courses:" + params[3] + ":page:" + pageNumber;
        } else if (originalCallback.contains(":by_category_courses:")) {
            backCallback = "admin:courses:by_category_courses:" + params[3] + ":page:" + pageNumber;
        } else if (originalCallback.contains(":search_page:")) {
            backCallback = "admin:courses:search_page:" + params[3] + ":" + pageNumber;
        } else {
            backCallback = "admin:courses:page:" + pageNumber;
        }

        String status = dto.isDeleted() ? "❌ O'chirilgan" : "✅ Aktiv";
        String text = String.format(
                "📚 *Kurs: %s*\n\n" +
                        "*ID:* `%d`\n" +
                        "*Kategoriya:* %s\n" +
                        "*Instruktor:* %s\n" +
                        "*Modullar soni:* %d ta\n" +
                        "*Status:* %s\n" +
                        "*Tavsif:* %s",
                escapeMarkdown(dto.getTitle()), dto.getId(), escapeMarkdown(dto.getCategoryName()),
                escapeMarkdown(dto.getInstructorName()), dto.getModuleCount(), status, escapeMarkdown(dto.getDescription())
        );
        InlineKeyboardMarkup keyboard = inlineKeyboardService.courseDetailMenu(courseId, backCallback);
        EditMessageText editMessage = sendMsg.editMessage(chatId, messageId, text.toString(), keyboard);
        editMessage.setParseMode("Markdown");
        onlineEducationBot.myExecute(editMessage);
    }


    private void sendCoursesMainMenu(Long chatId, Integer messageId) {
        String menuText = "📚 Kurslarni boshqarish bo'limi.";
        InlineKeyboardMarkup keyboard = inlineKeyboardService.coursesMainMenu();
        onlineEducationBot.myExecute(sendMsg.editMessage(chatId, messageId, menuText, keyboard));
    }

    private void initiateCourseSearch(Long chatId, Integer messageId) {
        telegramUserRepository.updateStateByChatId(chatId, UserState.ADMIN_AWAITING_COURSE_SEARCH_QUERY);
        String text = "Iltimos, qidirish uchun kurs nomini kiriting.";
        InlineKeyboardMarkup keyboard = inlineKeyboardService.createSingleButtonKeyboard("⬅️ Orqaga", "admin:courses:main_menu");
        onlineEducationBot.myExecute(sendMsg.editMessage(chatId, messageId, text, keyboard));
    }


    private void showCourseStats(Long chatId, Integer messageId) {
        long totalActiveCourses = courseRepository.count();
        String text = String.format("📊 *Kurslar Bo'yicha Statistika*\n\n" + "📚 *Jami aktiv kurslar soni:* %d ta", totalActiveCourses);
        InlineKeyboardMarkup keyboard = inlineKeyboardService.createSingleButtonKeyboard("⬅️ Orqaga", "admin:courses:main_menu");
        onlineEducationBot.myExecute(sendMsg.editMessage(chatId, messageId, text, keyboard));
    }

    private void handleCourseListView(Long chatId, Integer messageId, String[] params) {
        if (params.length < 4) return;
        String listType = params[3];

        switch (listType) {
            case "init" -> {
                String menuText = "Kurslarni qanday usulda ko'rmoqchisiz?";
                InlineKeyboardMarkup keyboard = inlineKeyboardService.courseBrowseMethodMenu("admin:courses:main_menu");
                onlineEducationBot.myExecute(sendMsg.editMessage(chatId, messageId, menuText, keyboard));
            }
            case "by_mentor" -> {
                int page = Integer.parseInt(params[5]);
                sendMentorsListPage(chatId, messageId, page);
            }
            case "by_mentor_courses" -> {
                Long mentorId = Long.parseLong(params[4]);
                Long categoryId = Long.parseLong(params[5]);
                int page = Integer.parseInt(params[6]);
                sendCoursesListPage(chatId, messageId, page, null, mentorId, categoryId);
            }
            case "by_category" -> { // <-- YANGI QISM
                int page = Integer.parseInt(params[5]);
                sendCategoriesListPage(chatId, messageId, page);
            }


        }
    }


    private void sendMentorsListPage(Long chatId, Integer messageId, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, 5, Sort.by("profile.firstName"));
        Page<User> mentorPage = userRepository.findAllByRole(Role.INSTRUCTOR, pageable);
        String text = "👨‍🏫 Mentorlardan birini tanlang (Sahifa: %d / %d)".formatted(pageNumber + 1, mentorPage.getTotalPages());
        String backCallback = "admin:courses:browse:init";
        InlineKeyboardMarkup keyboard = inlineKeyboardService.mentorsPageMenu(mentorPage, backCallback);
        onlineEducationBot.myExecute(sendMsg.editMessage(chatId, messageId, text, keyboard));
    }


    private void sendCoursesByMentorPage(Long chatId, Integer messageId, Long mentorId, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, 10, Sort.by("id"));


        Page<Course> coursePage = courseRepository.findAllByInstructorIdAndDeletedFalse(mentorId, pageable);

        User mentor = userRepository.findById(mentorId).orElse(null);
        String mentorName = (mentor != null && mentor.getProfile() != null)
                ? mentor.getProfile().getFirstName() + " " + mentor.getProfile().getLastName()
                : "Noma'lum mentor";


        StringBuilder text = new StringBuilder();
        text.append(String.format("📚 *%s* mentorining kurslari\n", escapeMarkdown(mentorName)));
        text.append(String.format("_Sahifa: %d / %d_\n\n", pageNumber + 1, coursePage.getTotalPages()));


        List<Course> coursesOnPage = coursePage.getContent();
        if (coursesOnPage.isEmpty()) {
            text.append("Bu mentorga tegishli aktiv kurslar topilmadi.");
        } else {
            for (int i = 0; i < coursesOnPage.size(); i++) {
                Course course = coursesOnPage.get(i);
                String categoryName = (course.getCategory() != null) ? course.getCategory().getName() : "Noma'lum";


                text.append(String.format("`%d.` 💻 **%s** — _%s_\n",
                        i + 1,
                        escapeMarkdown(course.getTitle()),
                        escapeMarkdown(categoryName)
                ));
            }
            text.append("\n🔽 Tanlash uchun tegishli tugmani bosing.");
        }

        InlineKeyboardMarkup keyboard = inlineKeyboardService.coursesPageMenu(coursePage, null, mentorId, null);


        EditMessageText editMessage = sendMsg.editMessage(chatId, messageId, text.toString(), keyboard);
        editMessage.setParseMode("Markdown");
        onlineEducationBot.myExecute(editMessage);
    }


    private void sendCategoriesListPage(Long chatId, Integer messageId, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, 5, Sort.by("name"));
        Page<Category> categoryPage = categoryRepository.findAll(pageable);
        String text = "🗂 Kategoriyalardan birini tanlang (Sahifa: %d / %d)".formatted(pageNumber + 1, categoryPage.getTotalPages());
        String backCallback = "admin:courses:browse:init";
        InlineKeyboardMarkup keyboard = inlineKeyboardService.categoriesPageMenu(categoryPage, backCallback);
        onlineEducationBot.myExecute(sendMsg.editMessage(chatId, messageId, text, keyboard));
    }

    private void sendCoursesByCategoryIdPage(Long chatId, Integer messageId, Long categoryId, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, 10, Sort.by("id"));


        Page<Course> coursePage = courseRepository.findAllByCategoryIdAndDeletedFalse(categoryId, pageable);

        Category category = categoryRepository.findById(categoryId).orElse(null);
        String categoryName = (category != null) ? category.getName() : "Noma'lum kategoriya";


        StringBuilder text = new StringBuilder();
        text.append(String.format("🗂 *'%s'* kategoriyasidagi kurslar\n", escapeMarkdown(categoryName)));
        text.append(String.format("_Sahifa: %d / %d_\n\n", pageNumber + 1, coursePage.getTotalPages()));


        List<Course> coursesOnPage = coursePage.getContent();
        if (coursesOnPage.isEmpty()) {
            text.append("Bu kategoriyaga tegishli aktiv kurslar topilmadi.");
        } else {
            for (int i = 0; i < coursesOnPage.size(); i++) {
                Course course = coursesOnPage.get(i);

                String instructorName = (course.getInstructor() != null && course.getInstructor().getProfile() != null)
                        ? course.getInstructor().getProfile().getFirstName()
                        : "Noma'lum";

                text.append(String.format("`%d.` 💻 **%s** — _%s_\n",
                        i + 1,
                        escapeMarkdown(course.getTitle()),
                        escapeMarkdown(instructorName)
                ));
            }
            text.append("\n🔽 Tanlash uchun tegishli tugmani bosing.");
        }


        InlineKeyboardMarkup keyboard = createCoursesPageMenuForCategory(coursePage, categoryId);


        EditMessageText editMessage = sendMsg.editMessage(chatId, messageId, text.toString(), keyboard);
        editMessage.setParseMode("Markdown");
        onlineEducationBot.myExecute(editMessage);
    }


    private InlineKeyboardMarkup createCoursesPageMenuForCategory(Page<Course> coursePage, Long categoryId) {
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();


        List<InlineKeyboardButton> numberButtonsRow = new ArrayList<>();
        for (int i = 0; i < coursePage.getContent().size(); i++) {
            Course course = coursePage.getContent().get(i);

            String callback = "admin:courses:view:" + course.getId() + ":" + "category" + ":" + categoryId + ":" +  coursePage.getNumber();

            numberButtonsRow.add(createButton(String.valueOf(i + 1), callback));
        }
        if (!numberButtonsRow.isEmpty()) {
            keyboardRows.add(numberButtonsRow);
        }


        List<InlineKeyboardButton> navRow = new ArrayList<>();
        String baseCallback = "admin:courses:by_category_courses:" + categoryId + ":page:";
        if (coursePage.hasPrevious()) {
            navRow.add(createButton("⬅️", baseCallback + (coursePage.getNumber() - 1)));
        }
        if (coursePage.hasNext()) {
            navRow.add(createButton("➡️", baseCallback + (coursePage.getNumber() + 1)));
        }
        if (!navRow.isEmpty()) {
            keyboardRows.add(navRow);
        }


        keyboardRows.add(List.of(createButton("⬅️ Orqaga", "admin:courses:by_category:page:0")));

        return new InlineKeyboardMarkup(keyboardRows);
    }

    private InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton(text);
        button.setCallbackData(callbackData);
        return button;
    }











    private void handleBroadcastCallbacks(CallbackQuery callbackQuery, String[] paramss) {
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();
        String[] params = data.split(":");
        if (params.length < 3) return;

        String action = params[2];

        switch (action) {
            case "type":

                processBroadcastTypeSelection(chatId, messageId, params[3]);
                break;
            case "target":

                processBroadcastTargetSelection(chatId, messageId, Role.valueOf(params[3]));
                break;
            case "send":

                processBroadcastSend(chatId, messageId, Role.valueOf(params[3]));
                break;
            case "cancel":

                processBroadcastCancel(chatId, messageId, params[3]);
                break;
        }
    }


    private void processBroadcastTypeSelection(Long chatId, Integer messageId, String type) {
        adminMessageServiceImpl.getBroadcastTypeCache().put(chatId, type);

        telegramUserRepository.updateStateByChatId(chatId, UserState.ADMIN_BROADCAST_AWAITING_TEXT);

        onlineEducationBot.myExecute(sendMsg.editMessage(chatId, messageId, "Iltimos, yuboriladigan xabar matnini kiriting:", null));
    }


    private void processBroadcastTargetSelection(Long chatId, Integer messageId, Role role) {
        String roleText = switch (role) {
            case STUDENT -> "barcha Talabalarga";
            case INSTRUCTOR -> "barcha Instruktorlarga";
            default -> "barcha foydalanuvchilarga (Talaba+Instruktor)";
        };
        String text = String.format("Haqiqatan ham ushbu xabarni %s yubormoqchimisiz?", roleText);

        String confirmCallback = "admin:broadcast:send:" + role.name();
        String cancelCallback = "admin:broadcast:cancel:back_to_targets";
        InlineKeyboardMarkup keyboard = inlineKeyboardService.confirmationMenu(confirmCallback, cancelCallback);

        onlineEducationBot.myExecute(sendMsg.editMessage(chatId, messageId, text, keyboard));
    }


    private void processBroadcastSend(Long chatId, Integer messageId, Role role) {

        String text = adminMessageServiceImpl.getBroadcastTextCache().get(chatId);
        String photoFileId = adminMessageServiceImpl.getBroadcastPhotoCache().get(chatId);


        adminMessageServiceImpl.getBroadcastTextCache().remove(chatId);
        adminMessageServiceImpl.getBroadcastPhotoCache().remove(chatId);
        telegramUserRepository.updateStateByChatId(chatId, UserState.ADMIN_MAIN_MENU);

        if (text == null || text.isBlank()) {
            log.error("Broadcast text not found in cache for admin {}", chatId);
            onlineEducationBot.myExecute(sendMsg.editMessage(chatId, messageId, "Xatolik: Yuboriladigan matn topilmadi."));
            return;
        }

        Set<Role> targetRoles = new HashSet<>();
        if (role == Role.ALL) {
            targetRoles.add(Role.STUDENT);
            targetRoles.add(Role.INSTRUCTOR);
        } else {
            targetRoles.add(role);
        }

        List<Long> targetChatIds = telegramUserRepository.findAllChatIdsByRoles(targetRoles);

        onlineEducationBot.myExecute(sendMsg.editMessage(chatId, messageId,
                String.format("✅ Xabar %d ta foydalanuvchiga yuborish uchun navbatga qo'yildi...", targetChatIds.size()), null));


        startBroadcasting(chatId, text, photoFileId, targetChatIds);
    }


    private void processBroadcastCancel(Long chatId, Integer messageId, String target) {
        if ("main".equals(target)) {

            telegramUserRepository.updateStateByChatId(chatId, UserState.ADMIN_MAIN_MENU);
            adminMessageServiceImpl.getBroadcastTextCache().remove(chatId);
            adminMessageServiceImpl.getBroadcastPhotoCache().remove(chatId);
            onlineEducationBot.myExecute(sendMsg.editMessage(chatId, messageId, "Amal bekor qilindi.", null));
        } else {

            String text = "Tanlov o'zgartirildi. Qayta tanlang:";
            onlineEducationBot.myExecute(sendMsg.editMessage(chatId, messageId, text, inlineKeyboardService.broadcastTargetRolesMenu()));
        }
    }


    @Async
    public void startBroadcasting(Long adminChatId, String text, String photoFileId, List<Long> targetChatIds) {
        log.info("Starting broadcast to {} users.", targetChatIds.size());
        int successCount = 0;
        int failureCount = 0;

        String htmlCaption = formatBroadcastMessageAsPost(text);

        for (Long targetChatId : targetChatIds) {
            try {
                if (photoFileId != null && !photoFileId.isBlank()) {
                    SendPhoto sendPhoto = new SendPhoto(String.valueOf(targetChatId), new InputFile(photoFileId));
                    sendPhoto.setCaption(htmlCaption);
                    sendPhoto.setParseMode("HTML");
                    onlineEducationBot.myExecute(sendPhoto);
                } else {
                    SendMessage sendMessage = new SendMessage(String.valueOf(targetChatId), text); // Matnli xabarni formatsiz yuboramiz
                    onlineEducationBot.myExecute(sendMessage);
                }
                successCount++;
            } catch (Exception e) {
                log.warn("Could not send broadcast message to chatId: {}", targetChatId);
                failureCount++;
            }
            try { Thread.sleep(35); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
        }

        String report = String.format("✅ Yuborish yakunlandi.\nMuvaffaqiyatli: %d\nXatolik: %d", successCount, failureCount);
        onlineEducationBot.myExecute(sendMsg.sendMessage(adminChatId, report));
    }


    private String formatBroadcastMessageAsPost(String rawText) {
        if (rawText == null || rawText.isBlank()) return "";
        String[] lines = rawText.split("\n", 2);
        String title = escapeHtml(lines[0]);
        String body = (lines.length > 1) ? escapeHtml(lines[1]) : "";
        return "<b>" + title + "</b>" + (!body.isEmpty() ? "\n\n" + body : "");
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }



}