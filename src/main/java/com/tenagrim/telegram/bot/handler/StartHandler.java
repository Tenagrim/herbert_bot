package com.tenagrim.telegram.bot.handler;

import com.tenagrim.telegram.bot.State;
import com.tenagrim.telegram.model.User;
import com.tenagrim.telegram.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import static com.tenagrim.telegram.util.TelegramUtil.createMessageTemplate;

@Component
@RequiredArgsConstructor
public class StartHandler implements Handler {
    private final UserRepository userRepository;

    @Override
    public List<PartialBotApiMethod<? extends Serializable>>  handle(User user, String message) {
        SendMessage welcomeMessage = createMessageTemplate(user)
                .setText("Введите пароль");
        user.setBotState(State.ENTER_PASS);
        userRepository.save(user);

        return List.of(welcomeMessage);
    }

    @Override
    public State operatedBotState() {
        return State.START;
    }

    @Override
    public List<String> operatedCallBackQuery() {
        return Collections.emptyList();
    }
}
