package com.tenagrim.telegram.repository;

import com.tenagrim.telegram.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    public List<Transaction> findAllByUser_ChatId(Integer chatId);
    public Optional<Transaction> findById(Integer id);
}
