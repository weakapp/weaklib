package tw.clotai.weaklib.net;

import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

public class NetHelper {

	private final static String FREEBBS_TW = ".freebbs.tw";
	
	/**
	 * for example, url is www.yahoo.com
	 * I will return .yahoo.com as my domain
	 */
	public static String getDomain(String url) {
		if (url == null) {
			return null;
		}
        String rurl = null;

		if (url.startsWith(".")) {
			return url;
		}

        if (url.startsWith("http")) {
            rurl = url;
        } else {
            rurl = "http://"+url;
        }

		Uri uri = Uri.parse(rurl);

		String host = uri.getHost();
		
		if (host == null) {
			return null;
		}
		
		int bIdx = host.lastIndexOf(FREEBBS_TW);
		if (bIdx >= 0) {
			if (host.equals("www"+FREEBBS_TW)) {
				return FREEBBS_TW;
			} else {
				return "."+host;
			}
		}

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
        return connected(context, false);
    }
	
	public static boolean connected(Context context, boolean wifi) {
		boolean ret = false;

		if (context == null) {
			return false;
		}
		ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo;

        if (wifi) {
            networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        } else {
            networkInfo = connMgr.getActiveNetworkInfo();
        }

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
		
		if (uri.getQuery() != null) {
			sb.append("?"+uri.getQuery());
		}
		return sb.toString();
	}
	
	public static String getBaseURL(String url) {
		Uri uri = Uri.parse(url);
		
		String path = uri.getPath();
		if ((path == null) || (path.trim().length() == 0)) {
			return url;
		} else {
			int index = url.lastIndexOf(path);
			if (index <= 0) {
				return url;
			}
			
			String ret  = url.substring(0,  index) +"/";
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
	
	public static String getCharset(String body) {
        //content="text/html; charset=big5-hkscs"
		String charset = "utf-8";
        if (body == null) {
            return charset;
        }

		//Pattern p = Pattern.compile(";\\s*charset=\\s*([^\"]+)/?>");
        Pattern p = Pattern.compile("text/html;\\s*charset=([^'/\\s\"]+)[^>]*>");
		Matcher m = p.matcher(body);
		if (m.find()) {
			charset = m.group(1);

			if (charset == null) {
				charset = "utf-8";
            }

            if (m.find()) {
                charset = m.group(1);
                if (charset == null) {
                    charset = "utf-8";
                }
            }
		}
		
		return charset.trim();
	}
	
}
