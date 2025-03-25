package com.springboot.banking_app.service.impl;

import com.springboot.banking_app.dto.BankResponse;
import com.springboot.banking_app.dto.UserRequest;

public interface UserService {

    BankResponse createAccount(UserRequest userRequest);

}
