package com.tenagrim.telegram.bot.handler;

import com.tenagrim.telegram.bot.State;
import com.tenagrim.telegram.exception.InvalidArgumentException;
import com.tenagrim.telegram.exception.LinkAlreadyExists;
import com.tenagrim.telegram.exception.NotFoundException;
import com.tenagrim.telegram.model.Transaction;
import com.tenagrim.telegram.model.User;
import com.tenagrim.telegram.model.Watcher;
import com.tenagrim.telegram.repository.TransactionRepository;
import com.tenagrim.telegram.repository.UserRepository;
import com.tenagrim.telegram.repository.WatcherRepository;
import lombok.RequiredArgsConstructor;
import org.mapstruct.control.MappingControl;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.tenagrim.telegram.util.TelegramUtil.createInlineKeyboardButton;
import static com.tenagrim.telegram.util.TelegramUtil.createMessageTemplate;

@Component
@RequiredArgsConstructor
public class AdminTransactionHandler implements Handler {
    private static final String ALL_USERS = "/all_users";
    private static final String WATCH_USER = "/watch_user";
    private static final String MENU = "/menu";
    private static final String USER_TR = "/user_tr";
    private static final String ACCEPT_TR = "/accept_tr";
    private static final String DECLINE_TR = "/decline_tr";
    private final UserRepository userRepository;
    private final WatcherRepository watcherRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public List<PartialBotApiMethod<? extends Serializable>> handle(User user, String message) {
            try {
                if (message.equalsIgnoreCase(ALL_USERS))
                    return all_users(user);
                else if (message.startsWith(WATCH_USER))
                    return watch_user(user, message);
                else if (message.startsWith(USER_TR))
                    return user_transactions(user, message);
                else if (message.startsWith(ACCEPT_TR))
                    return accept_tr(user, message);
                else if (message.startsWith(DECLINE_TR))
                    return decline_tr(user, message);
                else
                    return menu(user);
            } catch (InvalidArgumentException e ){
                return List.of(createMessageTemplate(user).setText("Неверное количество аргументов"));
            } catch (NotFoundException e) {
                return List.of(createMessageTemplate(user).setText("Пользователь с запрошенным id не найден"));
            } catch (LinkAlreadyExists e ){
                return List.of(createMessageTemplate(user).setText("Связь с этим пользователем уже установлена"));
            } catch (NumberFormatException e){
                return List.of(createMessageTemplate(user).setText("Неверный формат id"));
            } catch (RuntimeException e) {
                return List.of(createMessageTemplate(user).setText("Ошибка: " + e.getMessage()));
            }
    }

    private List<PartialBotApiMethod<? extends Serializable>> accept_tr(User user, String message){
        Transaction transaction = getTransactionFromCommand(message);
        transaction.setApproved(true);
        transactionRepository.save(transaction);
        return List.of(createMessageTemplate(user)
                .setText("Запись принята")
                .setReplyMarkup(new InlineKeyboardMarkup()
                        .setKeyboard(List.of( List.of(createInlineKeyboardButton("Все записи", USER_TR + " " + transaction.getUser().getChatId()))))));
    }

    private List<PartialBotApiMethod<? extends Serializable>> decline_tr(User user, String message){
        Transaction transaction = getTransactionFromCommand(message);
        transaction.setHidden(true);
        transactionRepository.save(transaction);
        return List.of(createMessageTemplate(user)
                .setText("Запись скрыта"));
    }

    private Transaction getTransactionFromCommand(String command){
        String [] args = command.split(" ");
        Optional<Transaction> transaction = transactionRepository.findById(Integer.parseInt(args[1]));
        if (transaction.isEmpty())
            throw new NotFoundException();
        return transaction.get();
    }

    private List<PartialBotApiMethod<? extends Serializable>> menu(User user) {
        return List.of( createMessageTemplate(user)
                .setText("Меню: ")
                .setReplyMarkup(new InlineKeyboardMarkup()
                        .setKeyboard(List.of(
                                List.of(createInlineKeyboardButton("Все пользователи", ALL_USERS))))));
    }

    private List<PartialBotApiMethod<? extends Serializable>> user_transactions(User user, String message){
        User subject = getUserFromCommand(message);
        List<Transaction> transactions = transactionRepository.findAllByUser_ChatId(subject.getChatId());
        String transactionsString = transactions.stream()
                .filter(t-> !t.isHidden())
                .filter(t-> !t.isCancelled())
                .map( t-> "id: " + t.getId() + " " + t.toString())
                .collect(Collectors.joining("\n"));
        double sum = transactions.stream()
                .filter(t-> !t.isHidden())
                .filter(t-> !t.isCancelled())
                .filter(Transaction::isApproved)
                .mapToDouble(Transaction::getValue)
                .sum();
        return List.of(createMessageTemplate(user).setText(transactionsString + "\n" + "Сумма: " + sum));
    }

    private List<PartialBotApiMethod<? extends Serializable>> all_users(User user) {
        List<User> users = userRepository.findAll();
        String usersString = users.stream()
                .map(Object::toString)
                .collect(Collectors.joining("\n"));
        return List.of(createMessageTemplate(user).setText(usersString));
    }

    private List<PartialBotApiMethod<? extends Serializable>> watch_user(User user, String message) {
        User subject = getUserFromCommand(message);
        if (watcherRepository.findByAdmin_ChatIdAndUser_ChatId(user.getChatId(), subject.getChatId()) != null)
            throw new LinkAlreadyExists();
        Watcher watcher = new Watcher(subject, user);
        watcherRepository.save(watcher);
        return List.of(createMessageTemplate(user).setText("Связь установлена"));
    }

    private User getUserFromCommand(String command){
        String [] args = command.split(" ");
        if (args.length != 2)
            throw new InvalidArgumentException();
        Optional<User> subject = userRepository.findByChatId(Integer.parseInt(args[1]));
        if (subject.isEmpty())
            throw new NotFoundException();
        return subject.get();
    }

    @Override
    public State operatedBotState() {
        return State.ADMIN;
    }

    @Override
    public List<String> operatedCallBackQuery() {
        return List.of(ALL_USERS, WATCH_USER, MENU);
    }
}
