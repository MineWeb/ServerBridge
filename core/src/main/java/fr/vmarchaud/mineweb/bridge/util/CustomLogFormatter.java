package fr.vmarchaud.mineweb.bridge.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class CustomLogFormatter extends Formatter {

    private final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    @Override
    public String format(LogRecord record) {
        String time = dateFormat.format(new Date(record.getMillis()));
        String source = record.getSourceClassName() + "." + record.getSourceMethodName();
        return time + " [" + source + "] " + record.getLevel() + " " + formatMessage(record) + System.lineSeparator();
    }
}
