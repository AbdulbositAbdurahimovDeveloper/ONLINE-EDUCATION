package uz.pdp.online_education.telegram.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import uz.pdp.online_education.enums.Role;
import uz.pdp.online_education.exceptions.EntityNotFoundException;
import uz.pdp.online_education.mapper.CourseMapper;
import uz.pdp.online_education.model.Course;
import uz.pdp.online_education.model.User;
import uz.pdp.online_education.model.UserProfile;
import uz.pdp.online_education.payload.course.CourseInfoDTO;
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
import java.util.Arrays;
import java.util.List;

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


    @Override
    public void handleCallback(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();

        User user = telegramUserRepository.findByChatId(chatId).orElseThrow().getUser();

        String[] params = data.split(":");
        if (!user.getRole().equals(Role.ADMIN)){
            return;
        }
//        if (!params[0].equals("admin") || !params[0].equals("auth")) return;

        String context = params[1];

        switch (context) {
            case Utils.CallbackData.ACTION_LOGOUT -> handleAuthCallback(user, chatId, messageId, params);
            case "users" -> handleUserCallbacks(chatId, messageId, data);
            case "courses" -> handleCourseCallbacks(chatId, messageId, data);
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
                // Kontekstni aniqlaymiz: qidiruvdan keyinmi yoki oddiy ro'yxatdanmi?
                // Hozircha oddiy, har doim ro'yxatga qaytadigan qilamiz.
                String backCallback = "admin:users:page:0";
                sendUserDetail(chatId, messageId, Long.parseLong(params[3]), backCallback);
            }
            case "main_menu" -> sendUsersMainMenu(chatId, messageId);
            case "search_init" -> initiateUserSearch(chatId, messageId);
            case "stats" -> showUserStats(chatId, messageId);
        }
    }

    // Bu metod endi 'searchTerm' ham qabul qiladi
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

        // Klaviatura yasashda 'searchTerm'ni berib yuboramiz. Bu PAGINATION uchun muhim!
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

    // Markdown maxsus belgilaridan qochish uchun yordamchi metod
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
            case "browse" -> handleCourseBrowseView(chatId, messageId,params);
            case "search_init" -> initiateCourseSearch(chatId, messageId);
            case "stats" -> showCourseStats(chatId, messageId);

            // SAHIFALASH
            case "page" -> sendCoursesListPage(chatId, messageId, Integer.parseInt(params[3]), null,null);
            case "search_page" -> sendCoursesListPage(chatId, messageId, Integer.parseInt(params[4]), params[3], null);
            case "list_by_mentor" -> sendMentorsListPage(chatId, messageId, Integer.parseInt(params[4]));
            case "list_by_mentor_courses" -> sendCoursesByMentorPage(chatId, messageId, Long.parseLong(params[3]), Integer.parseInt(params[5]));

            // KO'RISH
            case "view" -> {
                Long courseId = Long.parseLong(params[3]);

                String backCallback;

                // Endi biz callback'ning uzunligiga qarab, qayerdan kelganini aniqlaymiz.
                if (params.length > 5 && params[4].equals("search")) {
                    // Bu QIDIRUVdan kelgan, chunki formati: "admin:courses:view:ID:search:TERM:PAGE"
                    String searchTerm = params[5];
                    int page = Integer.parseInt(params[6]);
                    backCallback = "admin:courses:search_page:" + searchTerm + ":" + page;
                } else if (params.length > 5 && params[4].equals("mentor")) {
                    // Bu MENTORdan kelgan, chunki formati: "admin:courses:view:ID:mentor:MENTOR_ID:PAGE"
                    Long mentorId = Long.parseLong(params[5]);
                    int page = Integer.parseInt(params[6]);
                    backCallback = "admin:courses:list_by_mentor_courses:" + mentorId + ":page:" + page;
                } else {
                    // Bu ODDIY RO'YXATDAN kelgan, chunki formati: "admin:courses:view:ID:all:PAGE"
                    int page = Integer.parseInt(params[5]);
                    backCallback = "admin:courses:page:" + page;
                }

                sendCourseDetail(chatId, messageId, courseId, backCallback);
                break;
            }
            case "list" -> handleCourseListView(chatId,messageId,params);
        }
    }

    // AdminCallBackQueryServiceImpl.java

//    private void handleCourseCallbacks(Long chatId, Integer messageId, String data) {
//        String[] params = data.split(":");
//        if (params.length < 3) return;
//        String action = params[2];
//
//        switch (action) {
//            case "page":
//                // Bu oddiy ro'yxat sahifasi
//                sendCoursesListPage(chatId, messageId, Integer.parseInt(params[3]), null, null);
//                break;
//            case "search_page":
//                // Bu qidiruv natijalari sahifasi
//                sendCoursesListPage(chatId, messageId, Integer.parseInt(params[4]), params[3], null);
//                break;
//            case "list_by_mentor_courses":
//                // Bu mentorning kurslari sahifasi
//                sendCoursesListPage(chatId, messageId, Integer.parseInt(params[5]), null, Long.parseLong(params[3]));
//                break;
//
//            // --- MANA ENG MUHIM QISM ---
//            case "view":
//                Long courseId = Long.parseLong(params[3]);
//                String backCallback;
//
//                // Endi biz callback'ning uzunligiga qarab, qayerdan kelganini aniqlaymiz.
//                if (params.length > 5 && params[4].equals("search")) {
//                    // Bu QIDIRUVdan kelgan, chunki formati: "admin:courses:view:ID:search:TERM:PAGE"
//                    String searchTerm = params[5];
//                    int page = Integer.parseInt(params[6]);
//                    backCallback = "admin:courses:search_page:" + searchTerm + ":" + page;
//                } else if (params.length > 5 && params[4].equals("mentor")) {
//                    // Bu MENTORdan kelgan, chunki formati: "admin:courses:view:ID:mentor:MENTOR_ID:PAGE"
//                    Long mentorId = Long.parseLong(params[5]);
//                    int page = Integer.parseInt(params[6]);
//                    backCallback = "admin:courses:list_by_mentor_courses:" + mentorId + ":page:" + page;
//                } else {
//                    // Bu ODDIY RO'YXATDAN kelgan, chunki formati: "admin:courses:view:ID:all:PAGE"
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
                sendCoursesListPage(chatId, messageId, page, null, mentorId);
            }
        }
    }

// AdminCallBackQueryServiceImpl.java

    private void sendCoursesListPage(Long chatId, Integer messageId, int pageNumber, String searchTerm, Long mentorId) {
        Pageable pageable = PageRequest.of(pageNumber, 10, Sort.by("id"));
        Page<Course> coursePage;
        StringBuilder text = new StringBuilder();

        // --- 1-QADAM: Kerakli ma'lumotlarni bazadan samarali olish ---

        if (searchTerm != null && !searchTerm.isBlank()) {
            coursePage = courseRepository.searchActiveCoursesByTitleWithDetails(searchTerm, pageable);
            text.append(String.format("🔍 *'%s' bo'yicha topilgan natijalar*\n", escapeMarkdown(searchTerm)));
        } else if (mentorId != null) {
            coursePage = courseRepository.findAllByInstructorIdWithDetails(mentorId, pageable);
            User mentor = userRepository.findById(mentorId).orElseThrow(() -> new EntityNotFoundException("mentor not found"));
            // ... (mentor nomini olish logikasi) ...
            text.append(String.format("📚 *%s* mentorining kurslari\n", escapeMarkdown(mentor.getProfile().getFirstName())));
        } else {
            coursePage = courseRepository.findAllWithDetails(pageable);
            text.append("📖 *Barcha kurslar ro'yxati*\n");
        }

        text.append(String.format("_Sahifa: %d / %d_\n\n", pageNumber + 1, coursePage.getTotalPages()));

        // --- 2-QADAM: Ro'yxatni formatlash (BU ENDI XATOSIZ ISHLASHI KERAK) ---

        List<Course> coursesOnPage = coursePage.getContent();
        if (coursesOnPage.isEmpty()) {
            text.append("Bu yerda kurslar topilmadi.");
        } else {
            for (int i = 0; i < coursesOnPage.size(); i++) {
                Course course = coursesOnPage.get(i);
                String statusEmoji = course.isDeleted() ? "❌" : "✅";

                // Endi .getCategory() va .getName() xavfsiz, chunki ma'lumotlar allaqachon yuklangan.
                String categoryName = (course.getCategory() != null) ? course.getCategory().getName() : "Noma'lum";

                text.append(String.format("`%d.` %s **%s** — _%s_\n",
                        i + 1,
                        statusEmoji,
                        escapeMarkdown(course.getTitle()),
                        escapeMarkdown(categoryName)
                ));
            }
            text.append("\n🔽 Tanlash uchun tegishli tugmani bosing.");
        }

        InlineKeyboardMarkup keyboard = inlineKeyboardService.coursesPageMenu(coursePage, searchTerm, mentorId);



        EditMessageText editMessage = sendMsg.editMessage(chatId, messageId, text.toString(), keyboard);
        editMessage.setParseMode("Markdown");
        onlineEducationBot.myExecute(editMessage);
    }

    @Transactional(readOnly = true)
    protected void sendCourseDetail(Long chatId, Integer messageId, Long courseId, String backCallback) {
        Course course = courseRepository.findByIdWithModules(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));
        CourseInfoDTO dto = courseMapper.toInfoDto(course);

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
        // 1. Foydalanuvchi holatini "Kurs qidirish uchun ma'lumot kutish"ga o'tkazamiz
        telegramUserRepository.updateStateByChatId(chatId, UserState.ADMIN_AWAITING_COURSE_SEARCH_QUERY);

        // 2. Foydalanuvchiga nima qilish kerakligini aytamiz
        String text = "Iltimos, qidirish uchun kurs nomini kiriting.";

        // Orqaga qaytish tugmasini qo'shamiz
        InlineKeyboardMarkup keyboard = inlineKeyboardService.createSingleButtonKeyboard(
                "⬅️ Orqaga", "admin:courses:main_menu"
        );

        onlineEducationBot.myExecute(sendMsg.editMessage(chatId, messageId, text, keyboard));
    }


    private void showCourseStats(Long chatId, Integer messageId) {
        // 1. To'g'ridan-to'g'ri repository'dan kurslar sonini sanaymiz
        // @SQLRestriction(value="deleted=false") tufayli, bu faqat aktiv kurslarni sanaydi.
        long totalActiveCourses = courseRepository.count();

        // 2. Chiroyli matn yasaymiz
        String text = String.format(
                "📊 *Kurslar Bo'yicha Statistika*\n\n" +
                        "📚 *Jami aktiv kurslar soni:* %d ta",
                totalActiveCourses
        );

        // 3. Orqaga qaytish tugmasi bilan klaviatura yasaymiz
        InlineKeyboardMarkup keyboard = inlineKeyboardService.createSingleButtonKeyboard(
                "⬅️ Orqaga", "admin:courses:main_menu"
        );

        // 4. Xabarni tahrirlab, yuboramiz
        EditMessageText editMessage = sendMsg.editMessage(chatId, messageId, text, keyboard);
        editMessage.setParseMode("Markdown");
        onlineEducationBot.myExecute(editMessage);
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
                int page = Integer.parseInt(params[6]);
                sendCoursesListPage(chatId, messageId, page, null, mentorId);
            }
        }
    }

    // --- YANGI YORDAMCHI METODLAR ---
    private void sendMentorsListPage(Long chatId, Integer messageId, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, 5, Sort.by("profile.firstName"));
        Page<User> mentorPage = userRepository.findAllByRole(Role.INSTRUCTOR, pageable);
        String text = "👨‍🏫 Mentorlardan birini tanlang (Sahifa: %d / %d)".formatted(pageNumber + 1, mentorPage.getTotalPages());
        String backCallback = "admin:courses:browse:init"; // Tanlash menyusiga qaytish
        InlineKeyboardMarkup keyboard = inlineKeyboardService.mentorsPageMenu(mentorPage, backCallback);
        onlineEducationBot.myExecute(sendMsg.editMessage(chatId, messageId, text, keyboard));
    }

    // Bu metod tanlangan mentorning kurslari ro'yxatini chiqaradi
    private void sendCoursesByMentorPage(Long chatId, Integer messageId, Long mentorId, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, 10, Sort.by("id"));

        // 1. Ma'lumotlarni bazadan olamiz
        Page<Course> coursePage = courseRepository.findAllByInstructorIdAndDeletedFalse(mentorId, pageable);

        User mentor = userRepository.findById(mentorId).orElse(null);
        String mentorName = (mentor != null && mentor.getProfile() != null)
                ? mentor.getProfile().getFirstName() + " " + mentor.getProfile().getLastName()
                : "Noma'lum mentor";

        // 2. Xabar matnini yasashni boshlaymiz (sarlavha qismi)
        StringBuilder text = new StringBuilder();
        text.append(String.format("📚 *%s* mentorining kurslari\n", escapeMarkdown(mentorName)));
        text.append(String.format("_Sahifa: %d / %d_\n\n", pageNumber + 1, coursePage.getTotalPages()));

        // --- MANA O'SHA QOLIB KETGAN QISM ---
        // 3. Ro'yxatning o'zini formatlaymiz
        List<Course> coursesOnPage = coursePage.getContent();
        if (coursesOnPage.isEmpty()) {
            text.append("Bu mentorga tegishli aktiv kurslar topilmadi.");
        } else {
            for (int i = 0; i < coursesOnPage.size(); i++) {
                Course course = coursesOnPage.get(i);
                String categoryName = (course.getCategory() != null) ? course.getCategory().getName() : "Noma'lum";

                // Raqam, Kurs Nomi, Kategoriya
                text.append(String.format("`%d.` 💻 **%s** — _%s_\n",
                        i + 1,
                        escapeMarkdown(course.getTitle()),
                        escapeMarkdown(categoryName)
                ));
            }
            text.append("\n🔽 Tanlash uchun tegishli tugmani bosing.");
        }
        // --- FORMATLASH TUGADI ---

        // 4. "Aqlli" klaviaturani yasaymiz
        InlineKeyboardMarkup keyboard = inlineKeyboardService.coursesPageMenu(coursePage, null,mentorId);

        // 5. Xabarni tahrirlab, yuboramiz
        EditMessageText editMessage = sendMsg.editMessage(chatId, messageId, text.toString(), keyboard);
        editMessage.setParseMode("Markdown"); // Yoki MarkdownV2, agar escapeMarkdownV2 ishlatsangiz
        onlineEducationBot.myExecute(editMessage);
    }
}