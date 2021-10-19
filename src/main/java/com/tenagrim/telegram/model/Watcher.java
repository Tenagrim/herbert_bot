package com.tenagrim.telegram.model;

import lombok.AllArgsConstructor;
import lombok.Generated;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Watcher extends AbstractBaseEntity {
    @ManyToOne()
    @JoinColumn(name="user_id")
    private User user;

    @ManyToOne()
    @JoinColumn(name="admin_id")
    private User admin;
}
