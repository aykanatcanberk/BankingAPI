package com.springboot.banking_app.service.impl;

import com.springboot.banking_app.dto.*;

public interface UserService {

    BankResponse createAccount(UserRequest userRequest);
    BankResponse login(LoginDto loginDto);
    BankResponse balanceEnquiry(EnquiryRequest enquiryRequest);
    String nameEnquiry(EnquiryRequest enquiryRequest);
    BankResponse creditAccount(CreditRequest creditRequest);
    BankResponse debitAccount(CreditRequest creditRequest);
    BankResponse transferBalance(TransferRequest transferRequest);


}
