package com.newlin.util.logger;

import java.time.LocalDateTime;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFileFormatter extends Formatter
{
    @Override
    public String format(LogRecord record)
    {
        String message = "[" + getDateTimeFormatted() + "]" +
                " [" + record.getLoggerName() + "/" + record.getLevel().getName() + "] " +
                formatMessage(record);

        return message + "\n";
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