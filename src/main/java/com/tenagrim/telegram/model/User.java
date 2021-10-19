package com.tenagrim.telegram.model;

import com.tenagrim.telegram.bot.State;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Entity
@Table(name = "users", uniqueConstraints = {@UniqueConstraint(columnNames = "chat_id", name = "users_unique_chatid_idx")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends AbstractBaseEntity{
    @Column(name = "chat_id", unique = true, nullable = false)
    @NotNull
    private int chatId;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "bot_state", nullable = false)
    private State botState;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles;

    @Override
    public String toString() {
        return "User{" +
                "chatId: " + chatId +
                "username: '" + username + '\'' +
                "state: " + botState +
                '}';
    }

    public User(int chatId) {
        this.chatId = chatId;
        this.username = String.valueOf(chatId);
        this.botState = State.START;
    }
}
