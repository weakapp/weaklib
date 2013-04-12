package tw.clotai.weaklib.net;

import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.util.Log;
import android.webkit.CookieSyncManager;

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
			e.printStackTrace();
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
			HttpCookie cookie = null;
			
			Iterator<?> it = cookies.entrySet().iterator();
			
			while (it.hasNext()) {
				Entry<String, String> entry = (Entry<String, String>) (it.next());
				cookie = new HttpCookie(entry.getKey(), entry.getValue());
				store.add(new URI(domain), cookie);
			}
		} catch (URISyntaxException e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		}
	}
	
	/** Remove webview cookies **/
	public void reset() {
		android.webkit.CookieManager cookieManager = android.webkit.CookieManager.getInstance();
	    cookieManager.removeAllCookie();
	    CookieSyncManager.getInstance().sync();
	}
	
	public void synCookies(String url) {
	    
		CookieStore store = manager.getCookieStore();
		
		List<HttpCookie> cookies = null;

		try {
			String domain = NetHelper.getDomain(url);
			cookies = store.get(new URI(domain));

		    android.webkit.CookieManager cookieManager = android.webkit.CookieManager.getInstance();
		    cookieManager.removeExpiredCookie();
		    
		    
		    StringBuilder sb = null;
		 
	        for (HttpCookie cookie: cookies) {
	        	sb = new StringBuilder();
	        	sb.append(cookie.getName() + "=" + cookie.getValue());
	        	cookieManager.setCookie(domain, sb.toString());
	        }
		    
		    CookieSyncManager.getInstance().sync();  

		} catch (URISyntaxException e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		}
	}
	
}
