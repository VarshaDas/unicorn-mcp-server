package com.varshadas.mcpserver.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.DayOfWeek;

@Component
public class DateTimeTool {

    @Tool(description = "Get the current date and time in a given timezone. Defaults to UTC if no timezone is provided.")
    public String getCurrentDateTime(@ToolParam(description = "Timezone ID, e.g. America/New_York, Asia/Kolkata. Defaults to UTC.") String timezone) {
        ZoneId zone = (timezone == null || timezone.isBlank()) ? ZoneId.of("UTC") : ZoneId.of(timezone);
        LocalDateTime now = LocalDateTime.now(zone);
        return "Current date/time in %s: %s (%s)".formatted(
                zone, now.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy h:mm a")), zone);
    }

    @Tool(description = "Calculate the number of hours between now and a future date/time. Useful for checking cancellation policy deadlines.")
    public String hoursUntil(@ToolParam(description = "Future date-time in ISO format, e.g. 2025-07-20T14:00") String futureDateTime) {
        LocalDateTime future = LocalDateTime.parse(futureDateTime);
        long hours = ChronoUnit.HOURS.between(LocalDateTime.now(), future);
        return "%d hours from now (cancellation policy: full refund if 48+ hrs, 50%% refund if 24-48 hrs, no refund under 24 hrs)".formatted(hours);
    }

    @Tool(description = "Get the date for a relative day reference like 'this Saturday', 'next Friday', 'tomorrow'.")
    public String resolveRelativeDate(@ToolParam(description = "Day of week to resolve, e.g. Saturday, Friday, tomorrow") String dayReference) {
        LocalDate today = LocalDate.now();
        if (dayReference.equalsIgnoreCase("today")) return today.toString();
        if (dayReference.equalsIgnoreCase("tomorrow")) return today.plusDays(1).toString();

        try {
            var targetDay = DayOfWeek.valueOf(dayReference.toUpperCase());
            LocalDate next = today.with(TemporalAdjusters.next(targetDay));
            return "%s is %s".formatted(dayReference, next);
        } catch (IllegalArgumentException e) {
            return "Could not resolve '%s' to a date".formatted(dayReference);
        }
    }
}
