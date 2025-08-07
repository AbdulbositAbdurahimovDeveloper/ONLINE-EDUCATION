package uz.pdp.telegram.config.service.redirect;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import uz.pdp.telegram.config.service.template.TelegramInstructorService;

@Service
@RequiredArgsConstructor
public class TelegramInstructorServiceImpl implements TelegramInstructorService {
    /**
     * @param update 
     * @return
     */
    @Override
    public BotApiMethod<?> onUpdateResave(Update update) {
        return null;
    }
}
