package com.tenagrim.telegram.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Transaction extends AbstractBaseEntity{
    @Column(name = "value")
    private double value;
    @Column(name = "comment")
    private String comment;
    private boolean approved;
    private boolean hidden;
    private boolean cancelled;

    @ManyToOne()
    @JoinColumn(name="user_id")
    private User user;

    @Override
    public String toString() {
        return
        approved ? "✅" : "❌" + "  " + value + "    " + comment;
    }

    public Transaction(User user, double value, String comment) {
        this.value = value;
        this.comment = comment;
        this.user = user;
        approved = false;
        hidden = false;
        cancelled = false;
    }
}
