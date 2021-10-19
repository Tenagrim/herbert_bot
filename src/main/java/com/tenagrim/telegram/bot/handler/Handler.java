package com.tenagrim.telegram.bot.handler;

import com.tenagrim.telegram.bot.State;
import com.tenagrim.telegram.model.User;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;

import java.io.Serializable;
import java.util.List;

public interface Handler {
    // основной метод, который будет обрабатывать действия пользователя
    List<PartialBotApiMethod<? extends Serializable>> handle(User user, String message);
    State operatedBotState();
    // метод, который позволяет узнать, какие команды CallBackQuery мы можем обработать в этом классе
    List<String> operatedCallBackQuery();
}
