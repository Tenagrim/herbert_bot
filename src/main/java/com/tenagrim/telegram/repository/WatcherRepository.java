package com.tenagrim.telegram.repository;

import com.tenagrim.telegram.model.Watcher;
import org.hibernate.validator.constraints.SafeHtml;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.util.List;

@Repository
public interface WatcherRepository extends JpaRepository<Watcher, Integer> {
    Watcher findByAdmin_ChatIdAndUser_ChatId(@NotNull int admin_chatId, @NotNull int user_chatId);
    List<Watcher> findAllByUser_ChatId(@NotNull int user_chatId);
}
