package com.tenagrim.telegram.bot.handler;

import com.tenagrim.telegram.bot.State;
import com.tenagrim.telegram.model.Transaction;
import com.tenagrim.telegram.model.User;
import com.tenagrim.telegram.model.Watcher;
import com.tenagrim.telegram.repository.TransactionRepository;
import com.tenagrim.telegram.repository.UserRepository;
import com.tenagrim.telegram.repository.WatcherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.tenagrim.telegram.util.TelegramUtil.createInlineKeyboardButton;
import static com.tenagrim.telegram.util.TelegramUtil.createMessageTemplate;

@Component
@RequiredArgsConstructor
public class UserTransactionHandler implements Handler {
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final WatcherRepository watcherRepository;

    private static final String TR_ALL = "/all_transactions";

    @Override
    public List<PartialBotApiMethod<? extends Serializable>> handle(User user, String message) {
       if (message.equalsIgnoreCase(TR_ALL))
           return allTransactions(user);
       else
           return addTransaction(user, message);
    }

    private List<PartialBotApiMethod<? extends Serializable>> addTransaction (User user, String message){
        double value;
        String comment;
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> inlineKeyboardButtonsRowOne = List.of(
                createInlineKeyboardButton("Все записи", TR_ALL));

        inlineKeyboardMarkup.setKeyboard(List.of(inlineKeyboardButtonsRowOne));
        String[] parts = message.split(" ", 2);
        try {
            value = Double.parseDouble(parts[0]);
            comment = parts.length > 1 ? parts[1] : "";
        }catch (NumberFormatException e){
            return List.of(createMessageTemplate(user).setText("Неверный формат. (сумма комментарий)"));
        }
        Transaction t = new Transaction(user, value, comment);
        try {
            transactionRepository.save(t);
            List<SendMessage> messagesToSend = new ArrayList<>();
            messagesToSend.add(createMessageTemplate(user)
                    .setText(String.format("Запись схранена\nСумма: %f\nКомментарий: %s", value, comment))
                    .setReplyMarkup(inlineKeyboardMarkup));
            addMessageToWatcher(user, messagesToSend, t);
            return List.copyOf(messagesToSend);
        }catch (RuntimeException e) {
            e.printStackTrace();
            return List.of(createMessageTemplate(user)
                    .setText("Ошибка создания записи"));
        }
    }

    private void addMessageToWatcher(User user, List<SendMessage> messagesToSend, Transaction transaction){
        List<Watcher> watchers = watcherRepository.findAllByUser_ChatId(user.getChatId());
        for (Watcher w : watchers){
            messagesToSend.add(createMessageTemplate(w.getAdmin())
                    .setText("Входящая транзакция: \n" + transaction.toString())
                    .setReplyMarkup(new InlineKeyboardMarkup().setKeyboard(List.of(
                            List.of(createInlineKeyboardButton("✅", "/tr_accept " + transaction.getId().toString()),
                                    createInlineKeyboardButton("❌", "/tr_decline " + transaction.getId().toString())
                            )))));
        }
    }

    private List<PartialBotApiMethod<? extends Serializable>> allTransactions (User user) {
        List<Transaction> transactions = transactionRepository.findAllByUser_ChatId(user.getChatId());
        String transactionsString = transactions.stream()
                .filter(t-> !t.isHidden())
                .filter(t-> !t.isCancelled())
                .map(Object::toString)
                .collect(Collectors.joining("\n"));
        double sum = transactions.stream()
                .filter(t-> !t.isHidden())
                .filter(t-> !t.isCancelled())
                .filter(Transaction::isApproved)
                .mapToDouble(Transaction::getValue)
                .sum();
        return List.of(createMessageTemplate(user).setText(transactionsString + "\n" + "Сумма: " + sum));
    }

    @Override
    public State operatedBotState() {
        return State.USER;
    }

    @Override
    public List<String> operatedCallBackQuery() {
        return List.of(TR_ALL);
    }
}
