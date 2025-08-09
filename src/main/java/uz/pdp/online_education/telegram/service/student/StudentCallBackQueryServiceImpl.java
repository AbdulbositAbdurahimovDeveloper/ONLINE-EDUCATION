package uz.pdp.online_education.telegram.service.student;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import uz.pdp.online_education.telegram.service.student.template.StudentCallBackQueryService;

@Service
@RequiredArgsConstructor
public class StudentCallBackQueryServiceImpl implements StudentCallBackQueryService {

    /**
     * @param callbackQuery 
     * @return
     */
    @Override
    public void handleCallback(CallbackQuery callbackQuery) {
    }
}
