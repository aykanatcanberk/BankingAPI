package com.springboot.banking_app.service.impl;

import com.springboot.banking_app.dto.*;
import com.springboot.banking_app.entity.User;
import com.springboot.banking_app.exception.AccountNotFoundException;
import com.springboot.banking_app.exception.InsufficientBalanceException;
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

    @Autowired
    TransactionService transactionService;

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

    @Override
    public BankResponse balanceEnquiry(EnquiryRequest enquiryRequest) {

        boolean isAccountExist = userRepository.existsByAccountNumber(enquiryRequest.getAccountNumber());

        if (!isAccountExist) {
            throw new AccountNotFoundException();
        }
        User foundUser = userRepository.findByAccountNumber(enquiryRequest.getAccountNumber());
        return BankResponse.builder()
                .responseMessage("Your account has been successfully verified.")
                .accountInfo(AccountInfo.builder()
                        .accountBalance(foundUser.getAccountBalance())
                        .accountNumber(enquiryRequest.getAccountNumber())
                        .accountName(foundUser.getFirstName() + " " + foundUser.getLastName())
                        .build())
                .build();

    }

    @Override
    public String nameEnquiry(EnquiryRequest enquiryRequest) {

        boolean isAccountExist = userRepository.existsByAccountNumber(enquiryRequest.getAccountNumber());
        if (!isAccountExist) {
            throw new AccountNotFoundException();
        }

        User foundUser = userRepository.findByAccountNumber(enquiryRequest.getAccountNumber());
        return foundUser.getFirstName() + " " + foundUser.getLastName();

    }

    @Override
    public BankResponse creditAccount(CreditRequest creditRequest) {
        boolean isAccountExist = userRepository.existsByAccountNumber(creditRequest.getAccountNumber());
        if (!isAccountExist) {
            throw new AccountNotFoundException();
        }

        User creditUser = userRepository.findByAccountNumber(creditRequest.getAccountNumber());
        creditUser.setAccountBalance(creditUser.getAccountBalance().add(creditRequest.getAmount()));
        userRepository.save(creditUser);


        TransactionDto transactionDto = TransactionDto.builder()
                .accountNumber(creditUser.getAccountNumber())
                .transactionType("CREDIT")
                .amount(creditRequest.getAmount())
                .build();
        transactionService.saveTransaction(transactionDto);

        return BankResponse.builder()
                .responseMessage("Your credit has been added successfully.")
                .accountInfo(AccountInfo.builder()
                        .accountName(creditUser.getFirstName() + " " + creditUser.getLastName())
                        .accountBalance(creditUser.getAccountBalance())
                        .accountNumber(creditUser.getAccountNumber())
                        .build())
                .build();
    }

    @Override
    public BankResponse debitAccount(CreditRequest creditRequest) {
        boolean isAccountExist = userRepository.existsByAccountNumber(creditRequest.getAccountNumber());

        if (!isAccountExist) {
            throw new AccountNotFoundException();
        }

        User debitUser = userRepository.findByAccountNumber(creditRequest.getAccountNumber());

        if (debitUser.getAccountBalance().compareTo(creditRequest.getAmount()) < 0) {
            throw new InsufficientBalanceException();
        }



        debitUser.setAccountBalance(debitUser.getAccountBalance().subtract(creditRequest.getAmount()));
        userRepository.save(debitUser);

        TransactionDto transactionDto = TransactionDto.builder()
                .accountNumber(debitUser.getAccountNumber())
                .transactionType("CREDIT")
                .amount(creditRequest.getAmount())
                .build();
        transactionService.saveTransaction(transactionDto);

        return BankResponse.builder()
                .responseMessage("Debit operation has been successfully.")
                .accountInfo(AccountInfo.builder()
                        .accountName(debitUser.getFirstName() + " " + debitUser.getLastName())
                        .accountBalance(debitUser.getAccountBalance())
                        .accountNumber(debitUser.getAccountNumber())
                        .build())
                .build();
    }

    @Override
    public BankResponse transferBalance(TransferRequest transferRequest) {

        boolean isSenderAccountExist = userRepository.existsByAccountNumber(transferRequest.getFromAccountNumber());
        boolean isReceiverAccountExist = userRepository.existsByAccountNumber(transferRequest.getToAccountNumber());

        if (!isSenderAccountExist || !isReceiverAccountExist) {
            throw new AccountNotFoundException();
        }

        User sender = userRepository.findByAccountNumber(transferRequest.getFromAccountNumber());
        User receiver = userRepository.findByAccountNumber(transferRequest.getToAccountNumber());


        if (sender.getAccountBalance().compareTo(transferRequest.getAmount()) < 0) {
            throw new InsufficientBalanceException();
        }

        sender.setAccountBalance(sender.getAccountBalance().subtract(transferRequest.getAmount()));
        userRepository.save(sender);
        EmailDetails debitAlert = EmailDetails.builder()
                .subject("Debit Alert: Transaction Notification")
                .recipient(sender.getEmail())
                .messageBody("Dear " + sender.getFirstName() + ",\n\n" +
                        "A debit transaction of $" + transferRequest.getAmount() + " has been processed from your account.\n" +
                        "Account Number: " + sender.getAccountNumber() + "\n" +
                        "Remaining Balance: $" + sender.getAccountBalance() + "\n\n" +
                        "If you did not authorize this transaction, please contact customer support immediately.\n\n" +
                        "Best regards,\n XX Bank")
                .build();
        emailService.sendEmail(debitAlert);

        receiver.setAccountBalance(receiver.getAccountBalance().add(transferRequest.getAmount()));
        userRepository.save(receiver);
        EmailDetails creditAlert = EmailDetails.builder()
                .subject("Credit Alert: Funds Received")
                .recipient(receiver.getEmail())
                .messageBody("Dear " + receiver.getFirstName() + ",\n\n" +
                        "You have received a credit of $" + transferRequest.getAmount() + " in your account.\n" +
                        "Account Number: " + receiver.getAccountNumber() + "\n" +
                        "New Balance: $" + receiver.getAccountBalance() + "\n\n" +
                        "Thank you for banking with us!\n\n" +
                        "Best regards,\n XX Bank")
                .build();
        emailService.sendEmail(creditAlert);

        TransactionDto transactionDto = TransactionDto.builder()
                .accountNumber(receiver.getAccountNumber())
                .transactionType("CREDIT")
                .amount(transferRequest.getAmount())
                .build();
        transactionService.saveTransaction(transactionDto);

        return BankResponse.builder()
                .responseMessage("Transfer operation has been successfully completed.")
                .accountInfo(AccountInfo.builder()
                        .accountName(sender.getFirstName() + " " + sender.getLastName())
                        .accountBalance(sender.getAccountBalance())
                        .accountNumber(sender.getAccountNumber())
                        .build())
                .build();
    }
}