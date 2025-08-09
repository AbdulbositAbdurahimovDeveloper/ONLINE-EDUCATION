package uz.pdp.online_education.telegram.config.service.redirect;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import uz.pdp.online_education.telegram.config.service.template.TelegramStudentService;
import uz.pdp.online_education.telegram.service.student.template.StudentCallBackQueryService;
import uz.pdp.online_education.telegram.service.student.template.StudentMessageService;

@Service
@RequiredArgsConstructor
public class TelegramStudentServiceImpl implements TelegramStudentService {

    private final StudentMessageService studentMessageService;
    private final StudentCallBackQueryService studentCallBackQueryService;

    /**
     * @param update
     * @return
     */
    @Override
    public void onUpdateResave(Update update) {

        if (update.hasCallbackQuery()) {
             studentCallBackQueryService.handleCallback(update.getCallbackQuery());
        } else if (update.hasMessage()) {
             studentMessageService.handleMessage(update.getMessage());
        }
    }
}
