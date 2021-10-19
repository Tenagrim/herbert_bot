package com.tenagrim.telegram.bot;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Serializable;
import java.util.List;

@Slf4j
@Component
@Getter
@RequiredArgsConstructor
public class Bot extends TelegramLongPollingBot {
    @Value("${bot.name}")
    private String botUsername;
    @Value("${bot.token}")
    private String botToken;
    private final UpdateReceiver updateReceiver;

    @Override
    public void onUpdateReceived(Update update) {
        List<PartialBotApiMethod<? extends Serializable>> messagesToSend = updateReceiver.handle(update);
        if (messagesToSend != null && !messagesToSend.isEmpty()) {
            messagesToSend.forEach(response -> {
                if (response instanceof SendMessage) {
                    executeWithExceptionCheck((SendMessage) response);
                }
            });
        }
    }

    public void executeWithExceptionCheck(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("oops");
            e.printStackTrace();
        }
    }
}