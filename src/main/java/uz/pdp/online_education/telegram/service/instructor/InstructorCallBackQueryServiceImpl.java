package uz.pdp.online_education.telegram.service.instructor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import uz.pdp.online_education.telegram.service.instructor.template.InstructorCallBackQueryService;

@Service
@RequiredArgsConstructor
public class InstructorCallBackQueryServiceImpl implements InstructorCallBackQueryService {

    /**
     * @param callbackQuery 
     * @return
     */
    @Override
    public void handleCallback(CallbackQuery callbackQuery) {
    }
}
