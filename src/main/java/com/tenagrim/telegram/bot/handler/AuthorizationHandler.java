package com.tenagrim.telegram.bot.handler;

import com.tenagrim.telegram.bot.State;
import com.tenagrim.telegram.model.User;
import com.tenagrim.telegram.repository.RoleRepository;
import com.tenagrim.telegram.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.tenagrim.telegram.util.TelegramUtil.createInlineKeyboardButton;
import static com.tenagrim.telegram.util.TelegramUtil.createMessageTemplate;

@Component
@RequiredArgsConstructor
public class AuthorizationHandler implements Handler {
    @Value("${passwords.user}")
    private  String userPassword;// = "123";
    @Value("${passwords.admin}")
    private  String adminPassword;// = "1234";
    private final UserRepository userRepository;

    @Override
    public List<PartialBotApiMethod<? extends Serializable>> handle(User user, String message) {
        String response;
        if (message.equals(userPassword)){
            user.setBotState(State.USER);
            //user.setRoles(Set.of(roleRepository.findByName("USER").orElse(roleRepository.save(new Role("USER")))));
            response = "Пароль принят";
        } else if (message.equals(adminPassword)) {
            user.setBotState(State.ADMIN);
            //user.setRoles(Set.of(roleRepository.findByName("ADMIN").orElse(roleRepository.save(new Role("ADMIN")))));
            response = "admin access granted";
        } else
            response = "Неверный пароль. Возвращайтесь позже";
        userRepository.save(user);
        List<SendMessage> messages = new ArrayList<>();
        messages.add(createMessageTemplate(user).setText(response));
        addMessageToAdmin(user, messages);
        return List.copyOf(messages);
    }

    private void addMessageToAdmin(User user, List<SendMessage> messagesToSend){
        List<User> admins = userRepository.findAllByBotState(State.ADMIN);
        for(User a : admins){
            messagesToSend.add(createMessageTemplate(a)
                    .setText("Новый пользователь: \n" + user.getChatId())
                    .setReplyMarkup(new InlineKeyboardMarkup().setKeyboard(List.of(
                            List.of(createInlineKeyboardButton("Начать отслеживание записей", "/watch_user " + user.getChatId())
                            )))));
        }
    }

    @Override
    public State operatedBotState() {
        return State.ENTER_PASS;
    }

    @Override
    public List<String> operatedCallBackQuery() {
        return Collections.emptyList();
    }
}
