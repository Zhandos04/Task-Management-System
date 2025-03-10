package org.example.taskmanagementsystem.service;

public interface EmailService {
    void sendEmail(String to, String subject, String text);
}
