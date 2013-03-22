package tw.clotai.weaklib.net;

import android.net.Uri;

public class NetHelper {

	/**
	 * for example, url is www.yahoo.com
	 * I will return .yahoo.com as my domain
	 */
	public static String getDomain(String url) {
		
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
}
