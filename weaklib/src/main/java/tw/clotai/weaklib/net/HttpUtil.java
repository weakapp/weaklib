package tw.clotai.weaklib.net;

import android.util.Log;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Internal static utilities for handling data.
 */
public class HttpUtil {

    public static List<HttpCookie> processResponseHeaders(Map<String, List<String>> resHeaders) {
        String cookiev;
        List<HttpCookie> cs = new ArrayList<>();
        if (resHeaders.size() == 0) return cs;

        List<String> values;
        List<HttpCookie> cookies;
        MyCookieParser mCookieParser;

        for (Map.Entry<String, List<String>> entry : resHeaders.entrySet()) {
            String name = entry.getKey();
            if (name == null) continue;

            values = entry.getValue();
            if (!name.equalsIgnoreCase("Set-Cookie")) continue;

            for (String value : values) {
                if (value == null)
                    continue;

                Log.e("TTTTT", "raw: " + value);
                mCookieParser = new MyCookieParser(value);
                cookies = mCookieParser.parse();
                for (HttpCookie c : cookies) {
                    if (c.hasExpired() || c.getDiscard()) continue;

                    cookiev = c.getValue();
                    if (cookiev == null) cookiev = "";
                    c.setValue(cookiev);
                    cs.add(c);

                    Log.e("TTTTTTTT", c.getDomain() + ": " + c.getName() + " -> " + cookiev);
                }
            }
        }
        return cs;
    }
}
