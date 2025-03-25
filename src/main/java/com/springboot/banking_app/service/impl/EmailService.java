package com.springboot.banking_app.service.impl;

import com.springboot.banking_app.dto.EmailDetails;

public interface EmailService {
    void sendEmail(EmailDetails emailDetails);
}