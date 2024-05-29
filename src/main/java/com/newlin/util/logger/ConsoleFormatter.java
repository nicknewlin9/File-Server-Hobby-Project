package com.newlin.util.logger;

import java.time.LocalDateTime;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class ConsoleFormatter extends Formatter
{
    @Override
    public String format(LogRecord record)
    {
        String color = switch (record.getLevel().getName())
        {
            case "SEVERE" -> ConsoleColors.RED.getCode();
            case "WARNING" -> ConsoleColors.YELLOW.getCode();
            case "INFO","FINE", "FINER", "FINEST" -> ConsoleColors.BRIGHT_WHITE.getCode();
            default -> ConsoleColors.RESET.getCode();
        };

        String message = "[" + getDateTimeFormatted() + "]" +
                " [" + record.getLoggerName() + "/" + record.getLevel().getName() + "] " +
                formatMessage(record);

        return color + message + ConsoleColors.RESET.getCode() + "\n";
    }

    public static String getDateTimeFormatted()
    {
        String month = String.format("%02d", LocalDateTime.now().getMonthValue());
        String day = String.format("%02d", LocalDateTime.now().getDayOfMonth());
        String year = String.format("%02d", LocalDateTime.now().getYear());
        String date = month + "/" + day + "/" + year;

        String hour = String.format("%02d", LocalDateTime.now().getHour());
        String minute = String.format("%02d", LocalDateTime.now().getMinute());
        String second = String.format("%02d", LocalDateTime.now().getSecond());
        String time = hour + ":" + minute + ":" + second;

        return date + " " + time;
    }
}
