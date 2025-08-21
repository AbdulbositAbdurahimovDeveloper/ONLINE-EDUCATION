package uz.pdp.online_education.telegram.config.service.redirect;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import uz.pdp.online_education.telegram.config.service.template.TelegramInstructorService;
import uz.pdp.online_education.telegram.service.instructor.template.InstructorCallBackQueryService;
import uz.pdp.online_education.telegram.service.instructor.template.InstructorProcessMessageService;

@Service
@RequiredArgsConstructor
public class TelegramInstructorServiceImpl implements TelegramInstructorService {

    private final InstructorCallBackQueryService instructorCallBackQueryService;
    private final InstructorProcessMessageService instructorMessageService;

    /**
     * @param update
     * @return
     */
    @Override
    @Async
    public void onUpdateResave(Update update) {

        if (update.hasCallbackQuery()) {
             instructorCallBackQueryService.handleCallback(update.getCallbackQuery());
        } else if (update.hasMessage()) {
             instructorMessageService.handleMessage(update.getMessage());
        }

    }
}
