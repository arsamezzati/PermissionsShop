package com.fusionslab.permissionshop.utils;

/**
 * Utility methods for time operations
 */
public class TimeUtils {

    /**
     * Formats time in seconds to a readable string
     *
     * @param seconds Time in seconds
     * @return Formatted time
     */
    public static String formatTime(int seconds) {
        if (seconds <= 0) {
            return "0s";
        }

        int minutes = seconds / 60;
        int hours = minutes / 60;
        int days = hours / 24;

        hours %= 24;
        minutes %= 60;
        seconds %= 60;

        StringBuilder sb = new StringBuilder();

        if (days > 0) {
            sb.append(days).append("d ");
        }

        if (hours > 0) {
            sb.append(hours).append("h ");
        }

        if (minutes > 0) {
            sb.append(minutes).append("m ");
        }

        if (seconds > 0) {
            sb.append(seconds).append("s");
        }

        return sb.toString().trim();
    }

    /**
     * Formats time remaining to a readable string
     *
     * @param expirationTime Expiration timestamp
     * @return Formatted time remaining
     */
    public static String formatTimeRemaining(long expirationTime) {
        long now = System.currentTimeMillis();
        long remaining = expirationTime - now;

        if (remaining <= 0) {
            return "Expired";
        }

        long seconds = remaining / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        hours %= 24;
        minutes %= 60;
        seconds %= 60;

        StringBuilder sb = new StringBuilder();

        if (days > 0) {
            sb.append(days).append("d ");
        }

        if (hours > 0 || days > 0) {
            sb.append(hours).append("h ");
        }

        if (minutes > 0 || hours > 0 || days > 0) {
            sb.append(minutes).append("m ");
        }

        sb.append(seconds).append("s");

        return sb.toString();
    }

    /**
     * Parses a time string to seconds
     *
     * @param timeString Time string (e.g. "1d 2h 30m 15s")
     * @return Time in seconds
     */
    public static int parseTime(String timeString) {
        if (timeString == null || timeString.isEmpty()) {
            return 0;
        }

        int seconds = 0;

        // Match days
        if (timeString.contains("d")) {
            String[] split = timeString.split("d");
            String dayStr = split[0].trim().split(" ")[split[0].trim().split(" ").length - 1];
            try {
                seconds += Integer.parseInt(dayStr) * 86400;
            } catch (NumberFormatException ignored) {
            }
            timeString = split.length > 1 ? split[1] : "";
        }

        // Match hours
        if (timeString.contains("h")) {
            String[] split = timeString.split("h");
            String hourStr = split[0].trim().split(" ")[split[0].trim().split(" ").length - 1];
            try {
                seconds += Integer.parseInt(hourStr) * 3600;
            } catch (NumberFormatException ignored) {
            }
            timeString = split.length > 1 ? split[1] : "";
        }

        // Match minutes
        if (timeString.contains("m")) {
            String[] split = timeString.split("m");
            String minuteStr = split[0].trim().split(" ")[split[0].trim().split(" ").length - 1];
            try {
                seconds += Integer.parseInt(minuteStr) * 60;
            } catch (NumberFormatException ignored) {
            }
            timeString = split.length > 1 ? split[1] : "";
        }

        // Match seconds
        if (timeString.contains("s")) {
            String[] split = timeString.split("s");
            String secondStr = split[0].trim().split(" ")[split[0].trim().split(" ").length - 1];
            try {
                seconds += Integer.parseInt(secondStr);
            } catch (NumberFormatException ignored) {
            }
        }

        return seconds;
    }
}