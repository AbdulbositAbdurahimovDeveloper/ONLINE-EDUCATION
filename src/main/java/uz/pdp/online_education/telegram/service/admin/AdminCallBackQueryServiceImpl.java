package uz.pdp.online_education.telegram.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import uz.pdp.online_education.telegram.service.admin.template.AdminCallBackQueryService;

@Service
@RequiredArgsConstructor
public class AdminCallBackQueryServiceImpl implements AdminCallBackQueryService {
    /**
     * @param callbackQuery 
     * @return
     */
    @Override
    public BotApiMethod<?> handleCallback(CallbackQuery callbackQuery) {
        return null;
    }
}
