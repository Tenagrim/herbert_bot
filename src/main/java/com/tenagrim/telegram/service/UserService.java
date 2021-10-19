package com.tenagrim.telegram.service;

import com.tenagrim.telegram.dto.Message;
import lombok.RequiredArgsConstructor;
import org.jvnet.hk2.annotations.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
@RequiredArgsConstructor
public class UserService {
    @Value("${passwords.user}")
    private final String userPassword;
    @Value("${passwords.admin}")
    private final String adminPassword;

    public Message saveUser(){
        return new Message("User saved");
    }
}
