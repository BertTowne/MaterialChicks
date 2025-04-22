package com.berttowne.materialchicks.util;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Calendar;

public class TimeFormatter {

    private static final long MILLIS_IN_SECOND = 1000;
    private static final long MILLIS_IN_MINUTE = MILLIS_IN_SECOND * 60;
    private static final long MILLIS_IN_HOUR = MILLIS_IN_MINUTE * 60;
    private static final long MILLIS_IN_DAY = MILLIS_IN_HOUR * 24;
    private static final long MILLIS_IN_MONTH = MILLIS_IN_DAY * 30;
    private static final long MILLIS_IN_YEAR = MILLIS_IN_DAY * 365;

    public static @NotNull String formatTimeDifference(long millis, boolean fullWords) {
        StringBuilder sb = new StringBuilder();

        long years = millis / MILLIS_IN_YEAR;
        millis %= MILLIS_IN_YEAR;
        long months = millis / MILLIS_IN_MONTH;
        millis %= MILLIS_IN_MONTH;
        long days = millis / MILLIS_IN_DAY;
        millis %= MILLIS_IN_DAY;
        long hours = millis / MILLIS_IN_HOUR;
        millis %= MILLIS_IN_HOUR;
        long minutes = millis / MILLIS_IN_MINUTE;
        millis %= MILLIS_IN_MINUTE;
        long seconds = millis / MILLIS_IN_SECOND;

        appendTimeUnit(sb, years, fullWords ? " year" : "yr", fullWords);
        appendTimeUnit(sb, months, fullWords ? " month" : "mo", fullWords);
        appendTimeUnit(sb, days, fullWords ? " day" : "d", fullWords);
        appendTimeUnit(sb, hours, fullWords ? " hour" : "h", fullWords);
        appendTimeUnit(sb, minutes, fullWords ? " minute" : "m", fullWords);
        appendTimeUnit(sb, seconds, fullWords ? " second" : "s", fullWords);

        return sb.toString().trim();
    }

    public static @NotNull String formatTimeDifference(@NotNull Calendar start, @NotNull Calendar end, boolean fullWords) {
        long millis = end.getTimeInMillis() - start.getTimeInMillis();
        return formatTimeDifference(millis, fullWords);
    }

    public static @NotNull String formatTimeDifferenceFromNow(Calendar date, boolean fullWords) {
        Calendar now = Calendar.getInstance();
        return formatTimeDifference(date, now, fullWords);
    }

    private static void appendTimeUnit(StringBuilder sb, long value, String unit, boolean fullWords) {
        if (value > 0) {
            sb.append(value).append(unit);
            if (fullWords && value > 1) {
                sb.append("s");
            }
            sb.append(" ");
        }
    }

    public static long parseTimeDifference(@NotNull String time) {
        long millis = 0;
        String[] parts = time.split(" ");

        for (String part : parts) {
            if (part.endsWith("yr")) {
                millis += Long.parseLong(part.replace("yr", "")) * MILLIS_IN_YEAR;
            } else if (part.endsWith("mo")) {
                millis += Long.parseLong(part.replace("mo", "")) * MILLIS_IN_MONTH;
            } else if (part.endsWith("d")) {
                millis += Long.parseLong(part.replace("d", "")) * MILLIS_IN_DAY;
            } else if (part.endsWith("h")) {
                millis += Long.parseLong(part.replace("h", "")) * MILLIS_IN_HOUR;
            } else if (part.endsWith("m")) {
                millis += Long.parseLong(part.replace("m", "")) * MILLIS_IN_MINUTE;
            } else if (part.endsWith("s")) {
                millis += Long.parseLong(part.replace("s", "")) * MILLIS_IN_SECOND;
            }
        }

        return millis;
    }

    public static Duration parseDuration(String time) {
        return Duration.ofMillis(parseTimeDifference(time));
    }

}