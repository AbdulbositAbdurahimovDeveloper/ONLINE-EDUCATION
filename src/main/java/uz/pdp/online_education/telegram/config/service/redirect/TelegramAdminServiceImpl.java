package uz.pdp.online_education.telegram.config.service.redirect;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import uz.pdp.online_education.telegram.config.service.template.TelegramAdminService;
import uz.pdp.online_education.telegram.service.admin.template.AdminCallBackQueryService;
import uz.pdp.online_education.telegram.service.admin.template.AdminMessageService;

@Service
@RequiredArgsConstructor
public class TelegramAdminServiceImpl implements TelegramAdminService {

    private final AdminCallBackQueryService adminCallBackQueryService;
    private final AdminMessageService adminMessageService;

    /**
     * @param update 
     * @return
     */
    @Override
    public BotApiMethod<?> onUpdateResave(Update update) {

        if (update.hasCallbackQuery()) {
            return adminCallBackQueryService.handleCallback(update.getCallbackQuery());
        } else if (update.hasMessage()) {
            return adminMessageService.handleMessage(update.getMessage());
        }

        return null;
    }
}
