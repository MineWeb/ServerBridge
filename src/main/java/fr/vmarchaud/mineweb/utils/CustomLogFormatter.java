package fr.vmarchaud.mineweb.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class CustomLogFormatter extends Formatter {
	
    private final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");

	@Override
	public String format(LogRecord record) {
		 StringBuilder builder = new StringBuilder(1000);
	        builder.append(dateFormat.format(new Date(record.getMillis()))).append(" - ");
	        builder.append("[").append(record.getSourceClassName());
	        builder.append(record.getSourceMethodName()).append("] - ");
	        builder.append("[").append(record.getLevel()).append("] - ");
	        builder.append(formatMessage(record));
	        builder.append("\n");
	        return builder.toString();
	}
}
