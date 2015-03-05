package tw.clotai.weaklib.net;

import android.content.Context;
import android.util.Log;
import android.webkit.CookieSyncManager;

import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

public class CookieManager {

    private final static String TAG = "CookieManager";

    private volatile static CookieManager helper = null;

    private volatile static java.net.CookieManager manager = null;

    private Context mContext = null;

    public static CookieManager getInstance(Context c) {
        if (helper == null) {
            synchronized (CookieManager.class) {
                if (helper == null) {
                    helper = new CookieManager(c.getApplicationContext());
                }
            }
        }
        return helper;
    }

    private CookieManager(Context c) {
        mContext = c;
        manager = new java.net.CookieManager();
        CookieSyncManager.createInstance(mContext);

        /** reset all the cookies **/
        CookieStore store = manager.getCookieStore();
        store.removeAll();

        android.webkit.CookieManager cookieManager = android.webkit.CookieManager.getInstance();
        cookieManager.removeAllCookie();
        cookieManager.setAcceptCookie(true);
    }


    public void addCookies(String url, String key, String value) {
        if ((url == null) || (key == null) || (value == null)) {
            return;
        }

        String domain = NetHelper.getDomain(url);

        CookieStore store = manager.getCookieStore();

        try {
            HttpCookie cookie = new HttpCookie(key, value);
            store.add(new URI(domain), cookie);
        } catch (URISyntaxException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public void addCookies(String url, Map<String, String> cookies) {
        if ((url == null) || (cookies == null) || (cookies.size() == 0)) {
            return;
        }

        String domain = NetHelper.getDomain(url);

        CookieStore store = manager.getCookieStore();

        try {
            HttpCookie cookie;
            for (Map.Entry<String, String> entry : cookies.entrySet()) {
                cookie = new HttpCookie(entry.getKey(), entry.getValue());
                store.add(new URI(domain), cookie);
            }
        } catch (URISyntaxException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * Remove webview cookies *
     */
    public void reset() {
        CookieStore store = manager.getCookieStore();
        store.removeAll();

        android.webkit.CookieManager cookieManager = android.webkit.CookieManager.getInstance();
        cookieManager.removeAllCookie();
        CookieSyncManager.getInstance().sync();
    }

    public void synCookies(String url) {

        CookieStore store = manager.getCookieStore();

        List<HttpCookie> cookies;

        try {
            String domain = NetHelper.getDomain(url);
            cookies = store.get(new URI(domain));

            android.webkit.CookieManager cookieManager = android.webkit.CookieManager.getInstance();
            cookieManager.removeExpiredCookie();

            for (HttpCookie cookie : cookies) {
                cookieManager.setCookie(domain, cookie.getName() + "=" + cookie.getValue());
            }

            CookieSyncManager.getInstance().sync();

        } catch (URISyntaxException e) {
            Log.e(TAG, e.getMessage());
        }
    }

}
