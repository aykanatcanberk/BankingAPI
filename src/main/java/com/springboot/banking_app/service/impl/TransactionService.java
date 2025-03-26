package com.springboot.banking_app.service.impl;

import com.springboot.banking_app.dto.TransactionDto;

public interface TransactionService {
    void saveTransaction(TransactionDto transaction);
}
