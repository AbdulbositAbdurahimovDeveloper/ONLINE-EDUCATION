package uz.pdp.online_education.telegram.config.service.redirect;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import uz.pdp.online_education.telegram.config.service.template.TelegramInstructorService;
import uz.pdp.online_education.telegram.service.instructor.template.InstructorCallBackQueryService;
import uz.pdp.online_education.telegram.service.instructor.template.InstructorMessageService;

@Service
@RequiredArgsConstructor
public class TelegramInstructorServiceImpl implements TelegramInstructorService {

    private final InstructorCallBackQueryService instructorCallBackQueryService;
    private final InstructorMessageService instructorMessageService;

    /**
     * @param update
     * @return
     */
    @Override
    public BotApiMethod<?> onUpdateResave(Update update) {

        if (update.hasCallbackQuery()) {
            return instructorCallBackQueryService.handleCallback(update.getCallbackQuery());
        } else if (update.hasMessage()) {
            return instructorMessageService.handleMessage(update.getMessage());
        }

        return null;
    }
}
