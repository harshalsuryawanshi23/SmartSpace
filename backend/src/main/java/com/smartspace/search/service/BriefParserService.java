package com.smartspace.search.service;

import com.smartspace.search.dto.ParseBriefResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BriefParserService {

    public ParseBriefResponse parse(String text) {
        String lowerText = text.toLowerCase();
        
        String eventType = null;
        if (lowerText.contains("birthday")) eventType = "BIRTHDAY";
        else if (lowerText.contains("marriage") || lowerText.contains("wedding")) eventType = "MARRIAGE";
        else if (lowerText.contains("corporate") || lowerText.contains("meeting")) eventType = "CORPORATE";
        else if (lowerText.contains("workshop")) eventType = "WORKSHOP";

        Integer guests = null;
        Pattern guestPattern = Pattern.compile("(\\d+)\\s*(people|guests|persons)");
        Matcher guestMatcher = guestPattern.matcher(lowerText);
        if (guestMatcher.find()) {
            guests = Integer.parseInt(guestMatcher.group(1));
        }

        List<String> amenities = new ArrayList<>();
        if (lowerText.contains("ac") || lowerText.contains("air condition")) amenities.add("AC");
        if (lowerText.contains("parking")) amenities.add("PARKING");
        if (lowerText.contains("catering") || lowerText.contains("food")) amenities.add("CATERING");

        String date = null;
        // Check for relative words
        if (lowerText.contains("tomorrow")) {
            date = LocalDate.now().plusDays(1).toString();
        } else if (lowerText.contains("today")) {
            date = LocalDate.now().toString();
        }
        
        // Very basic dd/mm or yyyy-mm-dd
        Pattern datePattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})|(\\d{1,2}/\\d{1,2})");
        Matcher dateMatcher = datePattern.matcher(lowerText);
        if (dateMatcher.find()) {
            String found = dateMatcher.group();
            if (found.contains("-")) {
                date = found;
            } else {
                // assume dd/mm for current year
                String[] parts = found.split("/");
                int d = Integer.parseInt(parts[0]);
                int m = Integer.parseInt(parts[1]);
                try {
                    date = LocalDate.now().withMonth(m).withDayOfMonth(d).toString();
                } catch (Exception e) {
                    // ignore invalid date
                }
            }
        }

        String startTime = null;
        Pattern timePattern = Pattern.compile("(\\d{1,2})(am|pm)");
        Matcher timeMatcher = timePattern.matcher(lowerText.replaceAll("\\s+", ""));
        if (timeMatcher.find()) {
            int h = Integer.parseInt(timeMatcher.group(1));
            boolean pm = timeMatcher.group(2).equals("pm");
            if (pm && h < 12) h += 12;
            if (!pm && h == 12) h = 0;
            startTime = String.format("%02d:00", h);
        }

        Integer durationMinutes = null;
        Pattern durationPattern = Pattern.compile("(\\d+)\\s*(hour|hr)");
        Matcher durationMatcher = durationPattern.matcher(lowerText);
        if (durationMatcher.find()) {
            durationMinutes = Integer.parseInt(durationMatcher.group(1)) * 60;
        }

        return ParseBriefResponse.builder()
                .eventType(eventType)
                .guests(guests)
                .date(date)
                .startTime(startTime)
                .durationMinutes(durationMinutes)
                .amenities(amenities)
                .build();
    }
}
