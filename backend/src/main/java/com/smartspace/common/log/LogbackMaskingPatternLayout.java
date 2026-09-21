package com.smartspace.common.log;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogbackMaskingPatternLayout extends PatternLayout {

    private Pattern multilinePattern;
    private final StringBuilder maskBuilder = new StringBuilder();

    @Override
    public void start() {
        // Match standard patterns that might leak PII/Secrets in JSON or standard logs
        // Example: "email": "foo@example.com" -> "email": "f***@example.com"
        // "phone": "+919876543210" -> "phone": "******3210"
        // "otp": "123456" -> "otp": "******"
        // "refreshToken": "abc..." -> "refreshToken": "***"

        String[] patterns = {
                "\"email\"\\s*:\\s*\"([^\"]+)\"",
                "\"phone\"\\s*:\\s*\"([^\"]+)\"",
                "\"otp\"\\s*:\\s*\"([^\"]+)\"",
                "\"refreshToken\"\\s*:\\s*\"([^\"]+)\"",
                "email=([^\\s,]+)",
                "phone=([^\\s,]+)",
                "otp=([^\\s,]+)",
                "refreshToken=([^\\s,]+)"
        };

        this.multilinePattern = Pattern.compile(String.join("|", patterns), Pattern.MULTILINE);
        super.start();
    }

    @Override
    public String doLayout(ILoggingEvent event) {
        return maskMessage(super.doLayout(event));
    }

    private String maskMessage(String message) {
        if (this.multilinePattern == null) {
            return message;
        }

        StringBuilder sb = new StringBuilder(message);
        Matcher matcher = multilinePattern.matcher(sb);
        while (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                if (matcher.group(i) != null) {
                    mask(sb, matcher.start(i), matcher.end(i));
                }
            }
        }
        return sb.toString();
    }

    private void mask(StringBuilder sb, int start, int end) {
        String value = sb.substring(start, end);
        if (value.contains("@")) {
            // Mask email
            int atIndex = value.indexOf('@');
            if (atIndex > 1) {
                sb.replace(start + 1, start + atIndex, "*".repeat(atIndex - 1));
            }
        } else if (value.length() >= 4) {
            // Mask everything but last 4 digits
            sb.replace(start, end - 4, "*".repeat(end - start - 4));
        } else {
            // Full mask
            sb.replace(start, end, "*".repeat(end - start));
        }
    }
}
