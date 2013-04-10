package tw.clotai.weaklib.net;

import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

public class NetHelper {

	/**
	 * for example, url is www.yahoo.com
	 * I will return .yahoo.com as my domain
	 */
	public static String getDomain(String url) {

		if (url.startsWith(".")) {
			return url;
		}		
		Uri uri = Uri.parse(url);

		String host = uri.getHost();

		int firstidx = host.indexOf(".");
		int lastidx = host.lastIndexOf(".");

		if (firstidx == lastidx) {
			return ("."+host);
		} else {
			host = host.substring(firstidx);
		}
		return host;
	}
	
	public static boolean connected(Context context) {
		boolean ret = false;

		if (context == null) {
			return false;
		}
		ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

		if (networkInfo == null) {
			return ret;
		}
		if (networkInfo.isConnected()) {
			ret = true;
		}
		return ret;
	}
	
	public static String getEncodeURL(String url) {
		StringBuilder sb = new StringBuilder();
		Uri uri = Uri.parse(url);
		
		List<String> paths = uri.getPathSegments();
		
		String schema = uri.getScheme();
				
		if (schema == null) {
			return url;
		}
		
		int index = url.indexOf("/", schema.length()+3);

		sb.append(url.substring(0, index));
		for (String s: paths) {
			try {
				sb.append("/"+URLEncoder.encode(s, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				sb.append("/"+s);
			}
		}
		
		String query = uri.getQuery();
		if (query != null) {
			sb.append("?"+query);
		}
		
		return sb.toString();
	}
	
	public static String getBaseURL(String url) {
		Uri uri = Uri.parse(url);
		
		String path = uri.getPath();
		if (path == null) {
			return url;
		} else {
			int index = url.indexOf(path);
			
			String ret  = url.substring(0,  index);
			return ret;
		}
	}

	
	public static Map<String, String> parseCookies(String url, Map<String, List<String>> resHeaders) {
    	Map<String, String> cookies_map = new HashMap<String, String>();
    	
        for (Map.Entry<String, List<String>> entry : resHeaders.entrySet()) {
            String name = entry.getKey();
            if (name == null) {
                continue;
            }

            List<String> values = entry.getValue();
            if (name.equalsIgnoreCase("Set-Cookie")) {
                for (String value : values) {
                    if (value == null)
                        continue;

                    List<HttpCookie> cookies = HttpCookie.parse(value);
					for (HttpCookie cookie : cookies) {
						cookies_map.put(cookie.getName(), cookie.getValue());
					}
					
                }
                break;
            }
        }
        return cookies_map;
	}
	
}
