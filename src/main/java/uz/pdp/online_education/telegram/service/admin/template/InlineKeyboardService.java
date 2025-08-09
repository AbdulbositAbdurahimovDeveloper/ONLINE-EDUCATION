package uz.pdp.online_education.telegram.service.admin.template;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public interface InlineKeyboardService {
    InlineKeyboardMarkup welcomeFirstTime(Long chatId);

}
