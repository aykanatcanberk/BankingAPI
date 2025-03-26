package com.springboot.banking_app.service.impl;

import com.springboot.banking_app.dto.TransactionDto;
import com.springboot.banking_app.entity.Transaction;
import com.springboot.banking_app.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Override
    public void saveTransaction(TransactionDto dto) {
        Transaction transaction = Transaction.builder()
                .transactionType(dto.getTransactionType())
                .accountNumber(dto.getAccountNumber())
                .amount(dto.getAmount())
                .status("SUCCESS")
                .build();

        transactionRepository.save(transaction);
    }
}
