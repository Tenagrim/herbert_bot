package com.tenagrim.telegram.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Set;

@Entity
@Table(name = "roles")
@NoArgsConstructor
@Setter
@Getter
public class Role extends AbstractBaseEntity {
    private String name;
    @Transient
    @ManyToMany(mappedBy = "roles")
    private Set<User> users;
    public Role (String name){
        this.name = name;
    }
}
