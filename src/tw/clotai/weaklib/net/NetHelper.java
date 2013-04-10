package tw.clotai.weaklib.net;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

public class NetHelper {

	/**
	 * for example, url is www.yahoo.com
	 * I will return .yahoo.com as my domain
	 */
	public static String getDomain(String url) {
		
		Uri uri = Uri.parse(url);

		String host = uri.getHost();
		
		if (host == null) {
			if (url != null) {
				Log.e("NetHelper", url);
			}
			return null;
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
		
		if (uri.getQuery() != null) {
			sb.append("?"+uri.getQuery());
		}
		return sb.toString();
	}
}
