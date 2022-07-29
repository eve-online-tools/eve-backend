package de.ronnywalter.eve.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateFormatThreadSafe {

    private static final TimeZone TIME_ZONE = TimeZone.getTimeZone("GMT");

    private final String format;
    private final boolean lenient;

    private final ThreadLocal<DateFormat> df = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            SimpleDateFormat value = new SimpleDateFormat(format, new Locale("en"));
            value.setTimeZone(TIME_ZONE);
            value.setLenient(lenient);
            return value;
        }
    };

    public DateFormatThreadSafe(String format) {
        this(format, false);
    }

    public DateFormatThreadSafe(String format, boolean lenient) {
        this.format = format;
        this.lenient = lenient;
    }

    public Date parse(String dateString) throws ParseException {
        return df.get().parse(dateString);
    }

    public String format(final Object date) {
        return df.get().format(date);
    }

}
