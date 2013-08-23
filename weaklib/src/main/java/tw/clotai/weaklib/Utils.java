package tw.clotai.weaklib;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
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
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Toast;

public class Utils {
	private final static String TAG = "Utils";

	private static int OCTAL_MAX = 377;

	private static final String HASH_ALGORITHM = "MD5";
	private static final int RADIX = 10 + 26; // 10 digits + 26 letters

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static <T> void executeAsyncTask(AsyncTask<T, ?, ?> task,
			T... params) {
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
				sb.append("; " + entry.getKey() + "=" + entry.getValue());
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
		for (String param : params) {
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

	public static String version(Context context) {
		String version = null;
		try {
			version = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
		}
		return version;
	}

	public static void deviceResolution(Context ctxt, int[] resolution) {

		Display d = ((WindowManager) ctxt
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

		DisplayMetrics metrics = new DisplayMetrics();
		d.getMetrics(metrics);

		resolution[0] = metrics.widthPixels;
		resolution[1] = metrics.heightPixels;
	}

	public static String generateUniqueID(String url, boolean ext) {

		StringBuilder sb = new StringBuilder();

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

		sb.append(bi.toString(RADIX));

		if (ext) {
			Uri uri = Uri.parse(url);

			String lastseq = uri.getLastPathSegment();

			if (lastseq != null) {
				int index = lastseq.lastIndexOf(".");
				if (index != -1) {
					sb.append(lastseq.substring(index));
				}
			}
		}

		return sb.toString();
	}

	public static String getCahcePath(Context c) {
		return getCahcePath(c, null);
	}

	public static String getCahcePath(Context c, String suffix) {
		if (c == null) {
			return null;
		}

		File appCacheDir = null;
		File packageDir = null;

		/** try to cache on external storage **/
		if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
			File dataDir = new File(new File(
					Environment.getExternalStorageDirectory(), "Android"),
					"data");

			packageDir = new File(dataDir, c.getPackageName());

			if (suffix != null) {
				appCacheDir = new File(new File(packageDir, "cache"), suffix);
			} else {
				appCacheDir = new File(packageDir, "cache");
			}
		}

		if (appCacheDir == null) {
			if (suffix != null) {
				appCacheDir = new File(c.getCacheDir(), suffix);
			} else {
				appCacheDir = c.getCacheDir();
			}
		}

        if (appCacheDir == null) {
            return null;
        }

		if (!appCacheDir.exists()) {
			if (!appCacheDir.mkdirs()) {
				if (!appCacheDir.exists()) {
					appCacheDir = null;
				}
			}
		}
		
		if (appCacheDir == null) {
			return null;
		}
		return appCacheDir.getAbsolutePath();
	}
	
	
	public static String getImageCahcePath(Context c) {
		return getImageCahcePath(c, null);
	}

	public static synchronized String getImageCahcePath(Context c, String suffix) {
		if (c == null) {
			return null;
		}

		boolean external = false;
		File appCacheDir = null;
		File packageDir = null;

		/** try to cache on external storage **/
		if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
			File dataDir = new File(new File(
					Environment.getExternalStorageDirectory(), "Android"),
					"data");

			packageDir = new File(dataDir, c.getPackageName());

			if (suffix != null) {
				appCacheDir = new File(new File(new File(packageDir, "cache"),
						"images"), suffix);
			} else {
				appCacheDir = new File(new File(packageDir, "cache"), "images");
			}
			external = true;
		}

		if (appCacheDir == null) {
			if (suffix != null) {
				appCacheDir = new File(new File(c.getCacheDir(), "images"),
						suffix);
			} else {
				appCacheDir = new File(c.getCacheDir(), "images");
			}
		}

		if (!appCacheDir.exists()) {
			if (!appCacheDir.mkdirs()) {
				appCacheDir = null;
			} else {
				if (external) {
					try {
						new File(packageDir, ".nomedia").createNewFile();
					} catch (IOException e) {
					}
				}
			}
		}
		
		if (appCacheDir == null) {
			return null;
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
            if (fos != null) {
                if (fos.getFD() != null) {
                    fos.getFD().sync();
                }
            }

			if (bout != null) {
				bout.close();
			}
			if (fos != null) {
				fos.close();
			}
		}
	}

	public static String octalTranslate(final CharSequence input) {
		if (input == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		int index = 0;
		int length = input.length();

		while (index < length) {
			do {
				if (index < (length - 1) && input.charAt(index) == '\\'
						&& Character.isDigit(input.charAt(index + 1))) {
					// if (index < (length - 1) &&
					// Character.isDigit(input.charAt(index + 1))) {
					final int start = index + 1;

					int end = index + 2;
					while (end < length && Character.isDigit(input.charAt(end))) {
						end++;
						if (Integer.parseInt(input.subSequence(start, end)
								.toString(), 10) > OCTAL_MAX) {
							end--;
							break;
						}
					}
					// System.out.println("input.subSequence(start, end).toString():"+input.subSequence(start,
					// end).toString());
					sb.append("%"
							+ Integer.toHexString(Integer.parseInt(input
									.subSequence(start, end).toString(), 8)));
					index = end;
					break;
				}
				int t = input.charAt(index);
				sb.append("%" + Integer.toHexString(t));
				index++;
			} while (false);
		}

		try {
			String s = URLDecoder.decode(sb.toString(), "UTF-8");
			return s;
		} catch (UnsupportedEncodingException e) {
            e.printStackTrace();
		}

		return sb.toString();
	}
	
	
	public static String replaceIllegalChars(String s) {
		if (s == null) {
			return s;
		}
		String t = s.replaceAll("[!\\?]", "");
		
		return t;
	}

    public static boolean isLandscape(Context ctxt) {
        int orientation = ctxt.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            return false;
        }
        return true;
    }

    public static float getRealDimens(Context ctxt, int resId) {
        Display d = ((WindowManager) ctxt
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);

        float dimen = ctxt.getResources().getDimension(resId);

        return (dimen / metrics.scaledDensity);
    }


    public static boolean isImage(File f) {
        if (f == null) {
            return false;
        }
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String type = fileNameMap.getContentTypeFor(f.getAbsolutePath());
        if (type == null) {
            return false;
        }

        String s = type.toLowerCase(Locale.US);
        if (s.contains("image")) {
            return true;
        }
        return false;
    }


    public interface OnProcessCallback {
        public void onProcessFile(String fullpath);
    }

    public static void delete(File f, OnProcessCallback callback) {
        if (f.exists()) {
			if (f.isDirectory()) {
				File[] files = f.listFiles();
				if ((files != null) && (files.length > 0)) {
					int i;
					int count = files.length;
					for (i = 0; i < count; i++) {
						if (files[i].isDirectory()) {
							delete(files[i], callback);
						} else {
							files[i].delete();
						}
					}
				}
				if (callback != null) {
					callback.onProcessFile(f.getAbsolutePath());
				}
			}
            f.delete();
        }
    }

    public static void copyFiles(File src, File dst, OnProcessCallback callback) {
        if (src.exists()) {
            File of;
            InputStream in = null;
            File[] files = src.listFiles();
            if ((files != null) && (files.length > 0)) {
                for (File f: files) {
                    if (f.isFile()) {
                        of = new File(dst, f.getName());
                        try {
                            in = new FileInputStream(f);
                            Utils.copyToFile(in, of);
                            if (callback != null) {
                                callback.onProcessFile(of.getAbsolutePath());
                            }
                        } catch (IOException e) {
                        } finally {
                            if (in != null) {
                                try {
                                    in.close();
                                } catch (IOException e) {}
                            }
                            in = null;
                        }
                    } else if (f.isDirectory()) {
                        of = new File(dst, f.getName());
                        of.mkdirs();
                        if (callback != null) {
                            callback.onProcessFile(of.getAbsolutePath());
                        }
                        copyFiles(f, of, callback);
                    }
                }
            }
        }
    }

    /** return true is locked **/
    public static void lockOrientation(Activity activity, boolean locked) {
        if (activity == null) {
            return;
        }

        int s = activity.getRequestedOrientation();

        if (locked) {
            if (s == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
                Display display = ((WindowManager) activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
                int rotation = display.getRotation();
                int tempOrientation = activity.getResources().getConfiguration().orientation;
                int orientation = 0;
                switch(tempOrientation) {
                    case Configuration.ORIENTATION_LANDSCAPE:
                        if(rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90)
                            orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                        else
                            orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                        break;
                    case Configuration.ORIENTATION_PORTRAIT:
                        if(rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_270)
                            orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                        else
                            orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                }
                activity.setRequestedOrientation(orientation);
            }
        } else {
            if (s != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }
        }
    }

    /** return true is locked **/
    public static boolean toggleLockOrientation(Activity activity) {
        if (activity == null) {
            return false;
        }

        int s = activity.getRequestedOrientation();

        if (s != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            return false;
        } else {
            Display display = ((WindowManager) activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            int rotation = display.getRotation();
            int tempOrientation = activity.getResources().getConfiguration().orientation;
            int orientation = 0;
            switch(tempOrientation) {
                case Configuration.ORIENTATION_LANDSCAPE:
                    if(rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90)
                        orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    else
                        orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                case Configuration.ORIENTATION_PORTRAIT:
                    if(rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_270)
                        orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    else
                        orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
            }
            activity.setRequestedOrientation(orientation);

            return true;
        }
    }

	public static String getPrintStackString(Exception ex) {
		StringWriter errors = new StringWriter();
		PrintWriter p = null;
		String ret = null;
		try {
			p = new PrintWriter(errors);
			ex.printStackTrace(p);
			ret = errors.toString();
		} finally {
			if (p != null) {
				p.close();
			}
		}
		return ret;
	}

	public static String getPrintStackString(java.lang.OutOfMemoryError e) {
		StringWriter errors = new StringWriter();
		PrintWriter p = null;
		String ret = null;
		try {
			p = new PrintWriter(errors);
			e.printStackTrace(p);
			ret = errors.toString();
		} finally {
			if (p != null) {
				p.close();
			}
		}
		return ret;
	}

    public static boolean isTaskRunning(AsyncTask<?,?,?> task) {
        if (task == null) {
            return false;
        }

        if (task.getStatus() == AsyncTask.Status.FINISHED) {
            return false;
        }

        return true;
    }
}