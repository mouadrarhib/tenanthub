package com.tenanthub.notification.email;

public interface EmailService {

    void send(String to, String subject, String body);
}
