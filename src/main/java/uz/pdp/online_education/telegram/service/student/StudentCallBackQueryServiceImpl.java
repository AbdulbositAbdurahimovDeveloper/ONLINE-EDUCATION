package uz.pdp.online_education.telegram.service.student;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import uz.pdp.online_education.telegram.config.controller.OnlineEducationBot;
import uz.pdp.online_education.telegram.mapper.SendMsg;
import uz.pdp.online_education.telegram.service.student.template.StudentCallBackQueryService;
import uz.pdp.online_education.telegram.service.student.template.StudentMessageService;
import uz.pdp.online_education.telegram.service.student.template.StudentReplyKeyboardService;

/**
 * Handles all callback queries originating from the student panel's inline keyboards.
 * This class acts as a central router, delegating actions based on the parsed callback data.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentCallBackQueryServiceImpl implements StudentCallBackQueryService {

    private final StudentMessageService studentMessageService;
    private final OnlineEducationBot bot;
    private final SendMsg sendMsg;
    private final StudentReplyKeyboardService studentReplyKeyboardService;

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleCallback(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String queryData = callbackQuery.getData();
        String callbackQueryId = callbackQuery.getId();

        // Immediately answer the callback query to stop the loading animation on the user's screen.
        bot.myExecute(new AnswerCallbackQuery(callbackQueryId));

        try {
            String[] dataParts = queryData.split(":");
            if (dataParts.length < 2) {
                log.warn("Received invalid callback data format: {}", queryData);
                return;
            }

            String module = dataParts[0];
            String action = dataParts[1];

            switch (module) {
                case "mycourse" -> handleMyCourseCallbacks(action, dataParts, chatId, messageId);
                case "mymodule" -> handleMyModuleCallbacks(action, dataParts, chatId, messageId);
                case "mylesson" -> handleMyLessonCallbacks(action, dataParts, chatId, messageId);
                case "content" -> handleContentCallbacks(action, dataParts, chatId);
                case "lesson" -> handleLessonCallbacks(action, dataParts, callbackQueryId);
                case "student" -> {
                    if ("main_menu".equals(action))
                        handleBackToMainMenu(chatId, messageId, callbackQuery.getFrom().getUserName());
                }
                default -> log.warn("Unhandled callback module prefix: {}", module);
            }
        } catch (Exception e) {
            log.error("Error processing callback query: " + queryData, e);
            // Optionally, send a user-facing error message.
        }
    }

    private void handleMyCourseCallbacks(String action, String[] dataParts, Long chatId, Integer messageId) {
        if ("list".equals(action) && dataParts.length > 3) {
            int page = Integer.parseInt(dataParts[3]);
            studentMessageService.editMyCoursesPage(chatId, messageId, page);
        } else if ("view".equals(action) && dataParts.length > 2) {
            Long courseId = Long.parseLong(dataParts[2]);
            studentMessageService.sendCourseModulesPage(chatId, messageId, courseId);
        }
    }

    private void handleMyModuleCallbacks(String action, String[] dataParts, Long chatId, Integer messageId) {
        if ("view".equals(action) && dataParts.length > 2) {
            Long moduleId = Long.parseLong(dataParts[2]);
            studentMessageService.sendLessonListPage(chatId, messageId, moduleId);
        }
        // TODO: Handle "module:buy:{moduleId}" callback
    }

    private void handleMyLessonCallbacks(String action, String[] dataParts, Long chatId, Integer messageId) {
        if ("view".equals(action) && dataParts.length > 2) {
            Long lessonId = Long.parseLong(dataParts[2]);
            studentMessageService.sendLessonMainMenu(chatId, messageId, lessonId);
        }
    }

    private void handleContentCallbacks(String action, String[] dataParts, Long chatId) {
        if ("view".equals(action) && dataParts.length > 2) {
            Long contentId = Long.parseLong(dataParts[2]);
            studentMessageService.sendContent(chatId, contentId);
        }
    }

    private void handleLessonCallbacks(String action, String[] dataParts, String chatId) {
        if ("locked".equals(action)) {
            // Send a gentle notification to the user that the lesson is locked.
            AnswerCallbackQuery answer = new AnswerCallbackQuery();
            answer.setCallbackQueryId(chatId); // Requires passing the original callbackQueryId
            answer.setText("Bu darsni ko'rish uchun avval modulni sotib oling.");
            answer.setShowAlert(true); // Shows a pop-up alert
            bot.myExecute(answer);
        }
    }

    private void handleBackToMainMenu(Long chatId, Integer messageId, String username) {
        ReplyKeyboardMarkup replyKeyboardMarkup = studentReplyKeyboardService.studentMainMenu();
        bot.myExecute(sendMsg.deleteMessage(chatId, messageId));
        bot.myExecute(sendMsg.sendMessage(chatId, "Bosh menu",replyKeyboardMarkup));
        studentMessageService.sendWelcomeMessage(chatId, username);
    }
}