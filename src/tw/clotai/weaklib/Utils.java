package tw.clotai.weaklib;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.widget.Toast;

public class Utils {
	private final static String TAG = "Utils";

	private static final String HASH_ALGORITHM = "MD5";
	private static final int RADIX = 10 + 26; // 10 digits + 26 letters
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static <T> void executeAsyncTask(AsyncTask<T, ?, ?> task, T... params) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
		} else {
			task.execute(params);
		}
	}
	
	public static String encryptPass(String pass, String passphrase) {
		if ((passphrase == null) || (pass == null)) {
			return null;
		}
		
		String s = pass;
		try {
			SecretKey key = null;
			byte[] secretBytes = passphrase.getBytes("UTF8");

			DESKeySpec keySpec = new DESKeySpec(secretBytes);
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			key = keyFactory.generateSecret(keySpec);

			byte[] cleartext = pass.getBytes("UTF8");

			Cipher cipher = Cipher.getInstance("DES");

			cipher.init(Cipher.ENCRYPT_MODE, key);

			byte[] clearBytes = cipher.doFinal(cleartext);

			StringBuilder sb = new StringBuilder();

			int i = 0;
			int count = clearBytes.length;
			for (i = 0; i < count; i++) {
				sb.append(clearBytes[i] + "_");
			}
			s = sb.toString();

		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		} catch (BadPaddingException e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		}

		return s;
	}

	public static String dencryptPass(String pass, String passphrase) {
		String pw = pass;

		try {

			String[] spass = pass.split("_");
			byte[] epass = new byte[spass.length];
			int i = 0;
			int count = epass.length;
			for (i = 0; i < count; i++) {
				epass[i] = Byte.parseByte(spass[i]);
			}

			SecretKey key = null;

			byte[] secretBytes = passphrase.getBytes("UTF8");

			DESKeySpec keySpec = new DESKeySpec(secretBytes);
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			key = keyFactory.generateSecret(keySpec);

			Cipher cipher = Cipher.getInstance("DES");
			cipher.init(Cipher.DECRYPT_MODE, key);
			byte[] plainTextPwdBytes = cipher.doFinal(epass);
			pw = new String(plainTextPwdBytes, "UTF8");

		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		} catch (BadPaddingException e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		}

		return pw;
	}
	
	public static String md5(String data) {
		String hashtext = null;
		try {
			MessageDigest digester = MessageDigest.getInstance("MD5");
			digester.update(data.getBytes(Charset.forName("UTF8")));

			byte[] digest = digester.digest();

			BigInteger bigInt = new BigInteger(1, digest);

			hashtext = bigInt.toString(16);

			while (hashtext.length() < 32) {
				hashtext = "0" + hashtext;
			}
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		}

		return hashtext;
	}
	
	@SuppressWarnings("unchecked")
	public static String cookiesStr(Map<String, String> cookies) {
		StringBuilder sb = new StringBuilder();
		
		boolean bFirst = true;
		
		Iterator<?> it = cookies.entrySet().iterator();
		
		while (it.hasNext()) {
			Entry<String, String> entry = (Entry<String, String>) (it.next());

        	if (bFirst) {
        		bFirst = false;
        		sb.append(entry.getKey() + "=" + entry.getValue());
        	} else {
        		sb.append("; "+entry.getKey() + "=" + entry.getValue());
        	}
		}

		return sb.toString();
	}
	
	public static Map<String, String> cookiesMap(String cookies) {
		if (cookies == null) {
			return new HashMap<String, String>();
		}
		
		Map<String, String> map = new HashMap<String, String>();
		
		String[] params = cookies.split("; ");
		for (String param: params) {
			String[] set = param.split("=");
			if (set.length > 1) {
				map.put(set[0], set[1]);	
			}
		}

		return map;
	}
	
	public static void toast(Context ctxt, int res) {
		Toast.makeText(ctxt, res, Toast.LENGTH_SHORT).show();
	}
	
	public static void toast(Context ctxt, String s) {
		Toast.makeText(ctxt, s, Toast.LENGTH_SHORT).show();
	}
	
	public static void deviceResolution(Activity activity, int[] resolution) {
		Display display = activity.getWindowManager().getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);

		resolution[0] = metrics.widthPixels;
		resolution[1] = metrics.heightPixels;
	}
	
	
	public static String generateUniqueID(String url) {
		byte[] md5 = null;
		BigInteger bi = null;
		
		try {
			MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
			digest.update(url.getBytes());
			md5 = digest.digest();
			
			bi = new BigInteger(md5).abs();
			
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		}
		
		if (bi == null) {
			return null;
		}
		return bi.toString(RADIX);
	}
	
	public static String getImageCahcePath(Context c, String suffix) {
		if (c == null) {
			return null;
		}
		
		File appCacheDir = null;
		
		/** try to cache on external storage **/
		if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
			File dataDir = new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data");
			
			File packageDir = new File(dataDir, c.getPackageName());
			
			appCacheDir = new File(new File(new File(packageDir, "cache"), "images"), suffix);
			
			if (!appCacheDir.exists()) {
				if (!appCacheDir.mkdirs()) {
					appCacheDir = null;
				} else {
					try {
						new File(packageDir, ".nomedia").createNewFile();
					} catch (IOException e) {
					}
				}
			}
		}

		if (appCacheDir == null) {
			appCacheDir = new File(new File(c.getCacheDir(), "images"), suffix);
		}
		
		return appCacheDir.getAbsolutePath();
	}

	public static void copyToFile(InputStream in, File dest) throws IOException {
		FileOutputStream fos = null;
		BufferedInputStream bin = null;
		BufferedOutputStream bout = null;

		try {
			fos = new FileOutputStream(dest);
			bin = new BufferedInputStream(in, 8192);
			bout = new BufferedOutputStream(fos, 8192);

			byte[] rdata = new byte[8192];
			int count = 0;
			while ((count = bin.read(rdata)) != -1) {
				bout.write(rdata, 0, count);
			}
			bout.flush();

		} finally {
			if (bin != null) {
				bin.close();
			}
			if (fos.getFD() != null) {
				fos.getFD().sync();
			}
			if (bout != null) {
				bout.close();
			}
			if (fos != null) {
				fos.close();
			}
		}
	}
	
}
