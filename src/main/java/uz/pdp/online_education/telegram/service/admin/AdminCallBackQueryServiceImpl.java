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
            case "page" -> sendCoursesListPage(chatId, messageId, Integer.parseInt(params[3]), null);
            case "search_page" -> sendCoursesListPage(chatId, messageId, Integer.parseInt(params[4]), params[3]);

            // --- MANA O'SHA MUHIM O'ZGARISH ---
            case "view" -> {
                Long courseId = Long.parseLong(params[3]);
                String fromContext = params[4];
                String searchTerm = fromContext.equals("search") ? params[5] : null;
                int page = Integer.parseInt(fromContext.equals("search") ? params[6] : params[5]);

                // "Orqaga" tugmasi uchun to'g'ri manzilni yasaymiz
                String backCallback = (searchTerm == null)
                        ? "admin:courses:page:" + page // Agar oddiy ro'yxatdan kelgan bo'lsa
                        : "admin:courses:search_page:" + searchTerm + ":" + page; // Agar qidiruvdan kelgan bo'lsa

                sendCourseDetail(chatId, messageId, courseId, backCallback);
            }

            case "main_menu" -> sendCoursesMainMenu(chatId, messageId);
            case "search_init" -> initiateCourseSearch(chatId, messageId);
            case "stats" -> showCourseStats(chatId, messageId);
        }
    }

    private void sendCoursesListPage(Long chatId, Integer messageId, int pageNumber, String searchTerm) {
        Pageable pageable = PageRequest.of(pageNumber, 10, Sort.by("id"));
        Page<Course> coursePage = courseRepository.findAll(pageable); // Hozircha qidiruvsiz

        StringBuilder text = new StringBuilder("📚 *Kurslar ro'yxati*\n_Sahifa: %d / %d_\n\n".formatted(pageNumber + 1, coursePage.getTotalPages()));

        List<Course> coursesOnPage = coursePage.getContent();
        if (coursesOnPage.isEmpty()) {
            text.append("Kurslar topilmadi.");
        } else {
            for (int i = 0; i < coursesOnPage.size(); i++) {
                Course course = coursesOnPage.get(i);
                String statusEmoji = course.isDeleted() ? "❌" : "✅";
                text.append(String.format("`%d.` %s *%s*\n", i + 1, statusEmoji, escapeMarkdown(course.getTitle())));
            }
            text.append("\n🔽 Tanlash uchun tegishli tugmani bosing.");
        }

        InlineKeyboardMarkup keyboard = inlineKeyboardService.coursesPageMenu(coursePage, searchTerm);
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
}