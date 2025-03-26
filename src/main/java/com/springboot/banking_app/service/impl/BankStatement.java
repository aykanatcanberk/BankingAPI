package com.springboot.banking_app.service.impl;

import com.itextpdf.text.*;

import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.springboot.banking_app.dto.EmailDetails;
import com.springboot.banking_app.entity.User;
import com.springboot.banking_app.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

import com.itextpdf.text.pdf.PdfWriter;
import com.springboot.banking_app.entity.Transaction;
import com.springboot.banking_app.repository.TransactionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class BankStatement {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private EmailService emailService;
    public static final String FILE = "C:\\Temp\\statement.pdf";

    public List<Transaction> generateStatement(String accountNumber, String startDate, String endDate) throws DocumentException, FileNotFoundException {
        User user = userRepository.findByAccountNumber(accountNumber);
        String customerName = user.getFirstName() + " " + user.getLastName();

        LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE);
        LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE);

        List<Transaction> transactions = transactionRepository.findAll()
                .stream()
                .filter(transaction -> transaction.getAccountNumber().equals(accountNumber))
                .filter(transaction -> !transaction.getCreatedAt().isBefore(start) && !transaction.getCreatedAt().isAfter(end))
                .toList();

        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter.getInstance(document, new FileOutputStream(FILE));
        document.open();

        // Bank Header
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, BaseColor.WHITE);
        PdfPTable headerTable = new PdfPTable(1);
        PdfPCell bankName = new PdfPCell(new Phrase("XX BANK", titleFont));
        bankName.setBackgroundColor(BaseColor.DARK_GRAY);
        bankName.setHorizontalAlignment(Element.ALIGN_CENTER);
        bankName.setPadding(15);
        bankName.setBorder(Rectangle.NO_BORDER);
        headerTable.addCell(bankName);
        document.add(headerTable);

        document.add(new Paragraph("\n"));

        // Customer Info Table
        PdfPTable customerTable = new PdfPTable(2);
        customerTable.setWidthPercentage(100);
        customerTable.addCell(getCell("Customer Name:", PdfPCell.ALIGN_LEFT, true));
        customerTable.addCell(getCell(customerName, PdfPCell.ALIGN_LEFT, false));
        customerTable.addCell(getCell("Address:", PdfPCell.ALIGN_LEFT, true));
        customerTable.addCell(getCell(user.getAddress(), PdfPCell.ALIGN_LEFT, false));
        customerTable.addCell(getCell("Statement Period:", PdfPCell.ALIGN_LEFT, true));
        customerTable.addCell(getCell(startDate + " to " + endDate, PdfPCell.ALIGN_LEFT, false));
        document.add(customerTable);

        document.add(new Paragraph("\n"));

        // Transaction Table
        PdfPTable transactionTable = new PdfPTable(4);
        transactionTable.setWidthPercentage(100);
        transactionTable.setSpacingBefore(10f);
        transactionTable.addCell(getHeaderCell("Date"));
        transactionTable.addCell(getHeaderCell("Transaction Type"));
        transactionTable.addCell(getHeaderCell("Amount"));
        transactionTable.addCell(getHeaderCell("Status"));

        for (Transaction transaction : transactions) {
            transactionTable.addCell(getCell(transaction.getCreatedAt().toString(), PdfPCell.ALIGN_LEFT, false));
            transactionTable.addCell(getCell(transaction.getTransactionType(), PdfPCell.ALIGN_LEFT, false));
            transactionTable.addCell(getCell(transaction.getAmount().toString(), PdfPCell.ALIGN_LEFT, false));
            transactionTable.addCell(getCell(transaction.getStatus(), PdfPCell.ALIGN_LEFT, false));
        }

        document.add(transactionTable);
        document.close();

        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(user.getEmail())
                .subject("Account Statement")
                .messageBody("Dear " + user.getFirstName() + ",\n\n" +
                        "Please find attached your latest account statement.\n\n" +
                        "If you have any questions, feel free to contact our support team.\n\n" +
                        "Best regards,\n" +
                        "XX Bank")
                .attachment(FILE)
                .build();

        emailService.sendEmailWithAttechment(emailDetails);


        return transactions;
    }

    private PdfPCell getCell(String text, int alignment, boolean bold) {
        Font font = bold ? new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD) : new Font(Font.FontFamily.HELVETICA, 12);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        cell.setHorizontalAlignment(alignment);
        cell.setBorder(Rectangle.BOX);
        return cell;
    }

    private PdfPCell getHeaderCell(String text) {
        Font font = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(BaseColor.DARK_GRAY);
        cell.setPadding(5);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        return cell;
    }
}
