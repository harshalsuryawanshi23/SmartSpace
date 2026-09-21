package com.smartspace.notification.adapter;

import com.smartspace.notification.entity.NotificationChannel;
import com.smartspace.notification.entity.NotificationOutbox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MailHogEmailProvider implements NotificationAdapter {

    private final JavaMailSender mailSender;

    @Override
    public void send(NotificationOutbox outbox) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(outbox.getDestination());
        message.setSubject("SmartSpace Notification: " + outbox.getTemplateCode());
        message.setText("Payload: " + outbox.getPayload()); // Render template in a real app
        message.setFrom("noreply@smartspace.local");

        mailSender.send(message);
        log.info("Email sent to {}", outbox.getDestination());
    }

    @Override
    public boolean supports(NotificationChannel channel) {
        return channel == NotificationChannel.EMAIL;
    }
}
