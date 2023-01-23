package com.moonlight.shipbattle.logging;

import java.io.Writer;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.LogRecord;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.logging.Formatter;

class SBFormatter extends Formatter
{
    private final DateFormat dateFormat;
    private static final String format = "%s [%s] %s :: %s \n%s";
    
    SBFormatter() {
        this.dateFormat = new SimpleDateFormat("EEE, yyyy. MMM dd.  HH:mm:ss.SSS");
    }
    
    @Override
    public String format(final LogRecord record) {
        final Date date = new Date();
        date.setTime(record.getMillis());
        String source;
        if (record.getSourceClassName() != null) {
            source = record.getSourceClassName();
            if (record.getSourceMethodName() != null) {
                source = source + ":" + record.getSourceMethodName();
            }
        }
        else {
            source = record.getLoggerName();
        }
        final String message = this.formatMessage(record);
        String throwable = "";
        if (record.getThrown() != null) {
            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter(sw);
            pw.println();
            record.getThrown().printStackTrace(pw);
            pw.close();
            throwable = sw.toString();
        }
        return String.format("%s [%s] %s :: %s \n%s", this.dateFormat.format(date), record.getLevel().getName(), source, message, throwable);
    }
}
