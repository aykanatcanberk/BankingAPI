package com.springboot.banking_app.service.impl;

import com.springboot.banking_app.dto.AccountInfo;
import com.springboot.banking_app.dto.BankResponse;
import com.springboot.banking_app.dto.EmailDetails;
import com.springboot.banking_app.dto.UserRequest;
import com.springboot.banking_app.entity.User;
import com.springboot.banking_app.exception.UserAlreadyExistsException;
import com.springboot.banking_app.repository.UserRepository;
import com.springboot.banking_app.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    EmailService emailService;

    @Override
    public BankResponse createAccount(UserRequest userRequest) {

        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new UserAlreadyExistsException();
        }

        User user =  User.builder()
                .firstName(userRequest.getFirstName())
                .lastName(userRequest.getLastName())
                .gender(userRequest.getGender())
                .address(userRequest.getAddress())
                .state(userRequest.getState())
                .accountNumber(AccountUtils.gererateAccountNumber())
                .accountBalance(BigDecimal.ZERO)
                .email(userRequest.getEmail())
                .phoneNumber(String.valueOf(userRequest.getPhoneNumber()))
                .status("ACTIVE")
                .build();

        User savedUser = userRepository.save(user);

        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(savedUser.getEmail())
                .subject("ACCOUNT CREATION")
                .messageBody("Congratulations! Your Account Has been Successfully Created.\nYour Account Details: \n" +
                        "Account Name: " + savedUser.getFirstName() + " " + savedUser.getLastName() + " " + "\nAccount Number: " + savedUser.getAccountNumber())
                .build();
        emailService.sendEmail(emailDetails);

        return new BankResponse().builder()
                .responseMessage("Account created successfully.")
                .accountInfo(AccountInfo.builder()
                        .accountBalance(savedUser.getAccountBalance())
                        .accountNumber(savedUser.getAccountNumber())
                        .accountName(savedUser.getFirstName() + " " + savedUser.getLastName())
                        .build())
                .build();
    }
}
