package tw.clotai.weaklib.net;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by vincent on 3/4/15.
 */
final class HttpDate {

    /**
     * Most websites serve cookies in the blessed format. Eagerly create the parser to ensure such
     * cookies are on the fast path.
     */
    private static final ThreadLocal<DateFormat> STANDARD_DATE_FORMAT =
            new ThreadLocal<DateFormat>() {
                @Override protected DateFormat initialValue() {
                    DateFormat rfc1123 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
                    rfc1123.setTimeZone(TimeZone.getTimeZone("GMT"));
                    return rfc1123;
                }
            };

    /** If we fail to parse a date in a non-standard format, try each of these formats in sequence. */
    private static final String[] BROWSER_COMPATIBLE_DATE_FORMAT_STRINGS = new String[] {
            "EEEE, dd-MMM-yy HH:mm:ss zzz", // RFC 1036
            "EEE MMM d HH:mm:ss yyyy", // ANSI C asctime()
            "EEE, dd-MMM-yyyy HH:mm:ss z", "EEE, dd-MMM-yyyy HH-mm-ss z", "EEE, dd MMM yy HH:mm:ss z",
            "EEE dd-MMM-yyyy HH:mm:ss z", "EEE dd MMM yyyy HH:mm:ss z", "EEE dd-MMM-yyyy HH-mm-ss z",
            "EEE dd-MMM-yy HH:mm:ss z", "EEE dd MMM yy HH:mm:ss z", "EEE,dd-MMM-yy HH:mm:ss z",
            "EEE,dd-MMM-yyyy HH:mm:ss z", "EEE, dd-MM-yyyy HH:mm:ss z",

            /* RI bug 6641315 claims a cookie of this format was once served by www.yahoo.com */
            "EEE MMM d yyyy HH:mm:ss z", };

    private static final DateFormat[] BROWSER_COMPATIBLE_DATE_FORMATS =
            new DateFormat[BROWSER_COMPATIBLE_DATE_FORMAT_STRINGS.length];

    /** Returns the date for {@code value}. Returns null if the value couldn't be parsed. */
    public static Date parse(String value) {
        try {
            return STANDARD_DATE_FORMAT.get().parse(value);
        } catch (ParseException ignored) {
        }
        synchronized (BROWSER_COMPATIBLE_DATE_FORMAT_STRINGS) {
            for (int i = 0, count = BROWSER_COMPATIBLE_DATE_FORMAT_STRINGS.length; i < count; i++) {
                DateFormat format = BROWSER_COMPATIBLE_DATE_FORMATS[i];
                if (format == null) {
                    format = new SimpleDateFormat(BROWSER_COMPATIBLE_DATE_FORMAT_STRINGS[i], Locale.US);
                    BROWSER_COMPATIBLE_DATE_FORMATS[i] = format;
                }
                try {
                    return format.parse(value);
                } catch (ParseException ignored) {
                }
            }
        }
        return null;
    }

    /** Returns the string for {@code value}. */
    public static String format(Date value) {
        return STANDARD_DATE_FORMAT.get().format(value);
    }

    private HttpDate() {
    }
}
