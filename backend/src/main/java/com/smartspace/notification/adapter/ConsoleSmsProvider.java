package com.smartspace.notification.adapter;

import com.smartspace.notification.entity.NotificationChannel;
import com.smartspace.notification.entity.NotificationOutbox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ConsoleSmsProvider implements NotificationAdapter {

    @Override
    public void send(NotificationOutbox outbox) {
        // In a real app, integrate with Twilio/SNS etc.
        log.info("============== SMS SENT ==============");
        log.info("To: {}", outbox.getDestination());
        log.info("Template: {}", outbox.getTemplateCode());
        log.info("Payload: {}", outbox.getPayload()); // Warning: Payload might have OTP, don't log in prod
        log.info("======================================");
    }

    @Override
    public boolean supports(NotificationChannel channel) {
        return channel == NotificationChannel.SMS;
    }
}
