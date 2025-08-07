package uz.pdp.online_education.telegram.config.service.redirect;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import uz.pdp.online_education.telegram.config.service.template.TelegramStudentService;

@Service
@RequiredArgsConstructor
public class TelegramStudentServiceImpl implements TelegramStudentService {

    /**
     * @param update 
     * @return
     */
    @Override
    public BotApiMethod<?> onUpdateResave(Update update) {
        return null;
    }
}
