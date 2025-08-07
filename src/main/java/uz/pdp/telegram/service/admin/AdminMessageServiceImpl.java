package uz.pdp.telegram.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

@Service
@RequiredArgsConstructor
public class AdminMessageServiceImpl implements  AdminMessageService {
    /**
     * @param message 
     * @return
     */
    @Override
    public BotApiMethod<?> handleMessage(Message message) {
        return null;
    }
}
