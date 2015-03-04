package tw.clotai.weaklib.net;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by vincent on 3/4/15.
 */
public class MyCookieParser {
    private static final String ATTRIBUTE_NAME_TERMINATORS = ",;= \t";
    private static final String WHITESPACE = " \t";
    private final String input;
    private final String inputLowerCase;
    private int pos = 0;

    /*
     * The cookie's version is set based on an overly complex heuristic:
     * If it has an expires attribute, the version is 0.
     * Otherwise, if it has a max-age attribute, the version is 1.
     * Otherwise, if the cookie started with "Set-Cookie2", the version is 1.
     * Otherwise, if it has any explicit version attributes, use the first one.
     * Otherwise, the version is 0.
     */
    boolean hasExpires = false;
    boolean hasMaxAge = false;
    boolean hasVersion = false;

    MyCookieParser(String input) {
        this.input = input;
        this.inputLowerCase = input.toLowerCase(Locale.US);
    }

    public List<HttpCookie> parse() {
        List<HttpCookie> cookies = new ArrayList<>(2);

        // The RI permits input without either the "Set-Cookie:" or "Set-Cookie2" headers.
        boolean pre2965 = true;
        if (inputLowerCase.startsWith("set-cookie2:")) {
            pos += "set-cookie2:".length();
            pre2965 = false;
            hasVersion = true;
        } else if (inputLowerCase.startsWith("set-cookie:")) {
            pos += "set-cookie:".length();
        }

            /*
             * Read a comma-separated list of cookies. Note that the values may contain commas!
             *   <NAME> "=" <VALUE> ( ";" <ATTR NAME> ( "=" <ATTR VALUE> )? )*
             */
        while (true) {
            String name = readAttributeName(false);
            if (name == null) {
                if (cookies.isEmpty()) {
                    throw new IllegalArgumentException("No cookies in " + input);
                }
                return cookies;
            }

            if (!readEqualsSign()) {
                throw new IllegalArgumentException(
                        "Expected '=' after " + name + " in " + input);
            }
            String value = readAttributeValue(pre2965 ? ";" : ",;");

            HttpCookie cookie = new HttpCookie(name, value);
            cookie.setVersion(pre2965 ? 0 : 1);
            cookies.add(cookie);

                /*
                 * Read the attributes of the current cookie. Each iteration of this loop should
                 * enter with input either exhausted or prefixed with ';' or ',' as in ";path=/"
                 * and ",COOKIE2=value2".
                 */
            while (true) {
                skipWhitespace();
                if (pos == input.length()) {
                    break;
                }

                if (input.charAt(pos) == ',') {
                    pos++;
                    break; // a true comma delimiter; the current cookie is complete.
                } else if (input.charAt(pos) == ';') {
                    pos++;
                }

                String attributeName = readAttributeName(true);
                if (attributeName == null) {
                    continue; // for empty attribute as in "Set-Cookie: foo=Foo;;path=/"
                }

                    /*
                     * Since expires and port attributes commonly include comma delimiters, always
                     * scan until a semicolon when parsing these attributes.
                     */
                String terminators = pre2965
                        || "expires".equals(attributeName) || "port".equals(attributeName)
                        ? ";"
                        : ";,";
                String attributeValue = null;
                if (readEqualsSign()) {
                    attributeValue = readAttributeValue(terminators);
                }
                setAttribute(cookie, attributeName, attributeValue);
            }

            if (hasExpires) {
                cookie.setVersion(0);
            } else if (hasMaxAge) {
                cookie.setVersion(1);
            }
        }
    }

    private void setAttribute(HttpCookie cookie, String name, String value) {
        if (name.equals("comment") && cookie.getComment() == null) {
            cookie.setComment(value);
        } else if (name.equals("commenturl") && cookie.getCommentURL() == null) {
            cookie.setCommentURL(value);
        } else if (name.equals("discard")) {
            cookie.setDiscard(true);
        } else if (name.equals("domain") && cookie.getDomain() == null) {
            cookie.setDomain(value);
        } else if (name.equals("expires")) {
            hasExpires = true;
            if (cookie.getMaxAge() == -1L) {
                long maxage = 0;
                Date date = HttpDate.parse(value);
                if (date != null) {
                    maxage = (date.getTime() - System.currentTimeMillis()) / 1000;
                }
                cookie.setMaxAge(maxage);
            }
        } else if (name.equals("max-age") && cookie.getMaxAge() == -1L) {
            hasMaxAge = true;
            cookie.setMaxAge(Long.parseLong(value));
        } else if (name.equals("path") && cookie.getPath() == null) {
            cookie.setPath(value);
        } else if (name.equals("port") && cookie.getPortlist() == null) {
            cookie.setPortlist(value != null ? value : "");
        } else if (name.equals("secure")) {
            cookie.setSecure(true);
        } else if (name.equals("version") && !hasVersion) {
            cookie.setVersion(Integer.parseInt(value));
        }
    }

    /**
     * Returns the next attribute name, or null if the input has been
     * exhausted. Returns wth the cursor on the delimiter that follows.
     */
    private String readAttributeName(boolean returnLowerCase) {
        skipWhitespace();
        int c = find(ATTRIBUTE_NAME_TERMINATORS);
        String forSubstring = returnLowerCase ? inputLowerCase : input;
        String result = pos < c ? forSubstring.substring(pos, c) : null;
        pos = c;
        return result;
    }

    /**
     * Returns true if an equals sign was read and consumed.
     */
    private boolean readEqualsSign() {
        skipWhitespace();
        if (pos < input.length() && input.charAt(pos) == '=') {
            pos++;
            return true;
        }
        return false;
    }

    /**
     * Reads an attribute value, by parsing either a quoted string or until
     * the next character in {@code terminators}. The terminator character
     * is not consumed.
     */
    private String readAttributeValue(String terminators) {
        skipWhitespace();

            /*
             * Quoted string: read 'til the close quote. The spec mentions only "double quotes"
             * but RI bug 6901170 claims that 'single quotes' are also used.
             */
        if (pos < input.length() && (input.charAt(pos) == '"' || input.charAt(pos) == '\'')) {
            char quoteCharacter = input.charAt(pos++);

            char bracketCharacter = input.charAt(pos);
            if (bracketCharacter == '[') {
                // this is for 17K.
            } else {
                int closeQuote = input.indexOf(quoteCharacter, pos);
                if (closeQuote == -1) {
                    throw new IllegalArgumentException("Unterminated string literal in " + input);
                }
                String result = input.substring(pos, closeQuote);
                pos = closeQuote + 1;
                return result;
            }
        }

        int c = find(terminators);
        String result = input.substring(pos, c);
        pos = c;
        return result;
    }

    /**
     * Returns the index of the next character in {@code chars}, or the end
     * of the string.
     */
    private int find(String chars) {
        for (int c = pos; c < input.length(); c++) {
            if (chars.indexOf(input.charAt(c)) != -1) {
                return c;
            }
        }
        return input.length();
    }

    private void skipWhitespace() {
        for (; pos < input.length(); pos++) {
            if (WHITESPACE.indexOf(input.charAt(pos)) == -1) {
                break;
            }
        }
    }

}
