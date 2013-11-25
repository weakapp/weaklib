package tw.clotai.weaklib;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Toast;

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
import java.io.Writer;
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
import java.io.Writer;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

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
        } catch (InvalidKeyException e) {
            Log.e(TAG, e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, e.getMessage());
        } catch (InvalidKeySpecException e) {
            Log.e(TAG, e.getMessage());
        } catch (NoSuchPaddingException e) {
            Log.e(TAG, e.getMessage());
        } catch (IllegalBlockSizeException e) {
            Log.e(TAG, e.getMessage());
        } catch (BadPaddingException e) {
            Log.e(TAG, e.getMessage());
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
        if (url == null) {
            return null;
        }
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

    public interface CopyToFileProgress {
        public void onProgress(int count);
    }
    public static void copyToFile(InputStream in, File dest) throws IOException {
        copyToFile(in, dest, null);
    }

    public static void copyToFile(InputStream in, File dest, CopyToFileProgress callback) throws IOException {
        FileOutputStream fos = null;
        BufferedInputStream bin = null;
        BufferedOutputStream bout = null;

        int ucnt = 0;
        int sleepcnt = 0;

        try {
            fos = new FileOutputStream(dest);
            bin = new BufferedInputStream(in, 8192);
            bout = new BufferedOutputStream(fos, 8192);

            byte[] rdata = new byte[8192];
            int count;
            while ((count = bin.read(rdata)) != -1) {
                bout.write(rdata, 0, count);
                if (callback != null) {
                    sleepcnt++;
                    ucnt += count;
                    if ((sleepcnt % 5) == 0) {
                        callback.onProgress(ucnt);
                        SystemClock.sleep(10);
                    }
                }
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
        if (f == null) {
            return;
        }
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
            if (callback != null) {
                callback.onProcessFile(f.getAbsolutePath());
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
                for (File f : files) {
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
                                } catch (IOException e) {
                                }
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

    /**
     * return true is locked *
     */
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
                switch (tempOrientation) {
                    case Configuration.ORIENTATION_LANDSCAPE:
                        if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90)
                            orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                        else
                            orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                        break;
                    case Configuration.ORIENTATION_PORTRAIT:
                        if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_270)
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

    /**
     * return true is locked *
     */
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
            switch (tempOrientation) {
                case Configuration.ORIENTATION_LANDSCAPE:
                    if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90)
                        orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    else
                        orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                case Configuration.ORIENTATION_PORTRAIT:
                    if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_270)
                        orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    else
                        orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
            }
            activity.setRequestedOrientation(orientation);

            return true;
        }
    }

    public static void orientation_auto(Activity activity) {
        if (activity == null) {
            return;
        }
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    public static void orientation_locked(Activity activity) {
        if (activity == null) {
            return;
        }
        Display display = ((WindowManager) activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();
        int tempOrientation = activity.getResources().getConfiguration().orientation;
        int orientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        switch (tempOrientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90)
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                else
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_270)
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                else
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                break;
        }
        activity.setRequestedOrientation(orientation);
    }

    public static void orientation_portrait_locked(Activity activity) {
        if (activity == null) {
            return;
        }
        Display display = ((WindowManager) activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();
        int orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_270)
                orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            else
                orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
        }
        activity.setRequestedOrientation(orientation);
    }

    public static void orientation_landscape_locked(Activity activity) {
        if (activity == null) {
            return;
        }
        Display display = ((WindowManager) activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();
        int orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
        if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90)
                orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            else
                orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
        }
        activity.setRequestedOrientation(orientation);
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

    public static boolean isTaskRunning(AsyncTask<?, ?, ?> task) {
        if (task == null) {
            return false;
        }

        if (task.getStatus() == AsyncTask.Status.FINISHED) {
            return false;
        }

        return true;
    }


    public static String unescapeJava(String str) {
        if (str == null) {
            return null;
        }
        try {
            StringWriter writer = new StringWriter(str.length());
            unescapeJava(writer, str);
            return writer.toString();
        } catch (IOException ioe) {
            // this should never ever happen while writing to a StringWriter
            return null;
        }
    }

    private static void unescapeJava(Writer out, String str) throws IOException {
        if (out == null) {
            throw new IllegalArgumentException("The Writer must not be null");
        }
        if (str == null) {
            return;
        }
        int sz = str.length();

        StringBuilder unicode = new StringBuilder(4);
        boolean hadSlash = false;
        boolean inUnicode = false;
        for (int i = 0; i < sz; i++) {
            char ch = str.charAt(i);
            if (inUnicode) {
                // if in unicode, then we're reading unicode
                // values in somehow
                unicode.append(ch);
                if (unicode.length() == 4) {
                    // unicode now contains the four hex digits
                    // which represents our unicode character
                    try {
                        int value = Integer.parseInt(unicode.toString(), 16);
                        out.write((char) value);
                        unicode.setLength(0);
                        inUnicode = false;
                        hadSlash = false;
                    } catch (NumberFormatException nfe) {

                    }
                }
                continue;
            }
            if (hadSlash) {
                // handle an escaped value
                hadSlash = false;
                switch (ch) {
                    case '\\':
                        out.write('\\');
                        break;
                    case '\'':
                        out.write('\'');
                        break;
                    case '\"':
                        out.write('"');
                        break;
                    case 'r':
                        out.write('\r');
                        break;
                    case 'f':
                        out.write('\f');
                        break;
                    case 't':
                        out.write('\t');
                        break;
                    case 'n':
                        out.write('\n');
                        break;
                    case 'b':
                        out.write('\b');
                        break;
                    case 'u': {
                        // uh-oh, we're in unicode country....
                        inUnicode = true;
                        break;
                    }
                    default:
                        out.write(ch);
                        break;
                }
                continue;
            } else if (ch == '\\') {
                hadSlash = true;
                continue;
            }
            out.write(ch);
        }
        if (hadSlash) {
            // then we're in the weird case of a \ at the end of the
            // string, let's output it anyway.
            out.write('\\');
        }
    }


    public static String escapeJavascriptStyle(String str) {
        if (str == null) {
            return null;
        }
        try {
            StringWriter writer = new StringWriter(str.length() * 2);
            escapeJavaStyleString(writer, str, true);
            writer.flush();
            return writer.toString();
        } catch (IOException ioe) {
            return null;
        }
    }

    private static void escapeJavaStyleString(Writer out, String str, boolean escapeSingleQuote) throws IOException {
        if (out == null) {
            throw new IllegalArgumentException("The Writer must not be null");
        }

        if (str == null) {
            return;
        }
        int sz;
        sz = str.length();
        for (int i = 0; i < sz; i++) {
            char ch = str.charAt(i);

            // handle unicode
            if (ch > 0xfff) {
                out.write("%25u" + hex(ch));
            } else if (ch > 0xff) {
                out.write("%25u0" + hex(ch));
            } else if (ch > 0x7f) {
                out.write("%25u00" + hex(ch));
            } else if (ch < 32) {
                switch (ch) {
                    case '\b':
                        out.write('\\');
                        out.write('b');
                        break;
                    case '\n':
                        out.write('\\');
                        out.write('n');
                        break;
                    case '\t':
                        out.write('\\');
                        out.write('t');
                        break;
                    case '\f':
                        out.write('\\');
                        out.write('f');
                        break;
                    case '\r':
                        out.write('\\');
                        out.write('r');
                        break;
                    default:
                        if (ch > 0xf) {
                            out.write("%25u00" + hex(ch));
                        } else {
                            out.write("%25u000" + hex(ch));
                        }
                        break;
                }
            } else {
                switch (ch) {
                    case '\'':
                        if (escapeSingleQuote) {
                            out.write('\\');
                        }
                        out.write('\'');
                        break;
                    case '"':
                        out.write('\\');
                        out.write('"');
                        break;
                    case '\\':
                        out.write('\\');
                        out.write('\\');
                        break;
                    default:
                        out.write(ch);
                        break;
                }
            }
        }
    }

    private static String hex(char ch) {
        return Integer.toHexString(ch).toUpperCase();
    }

    private static final String ALLOWED_CHARACTERS = "0123456789qwertyuiopasdfghjklzxcvbnm";

    public static String randomStr(int len) {
        final Random random = new Random();
        final StringBuilder sb = new StringBuilder();
        int clen = ALLOWED_CHARACTERS.length();
        for (int i = 0; i < len; ++i) {
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(clen)));
        }
        return sb.toString();
    }

    public static boolean isAutoBrightnessOn(Context ctxt) {
        boolean automicBrightness;
        try {
            automicBrightness = Settings.System.getInt(ctxt.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        } catch (Settings.SettingNotFoundException e) {
            automicBrightness = false;
        }
        return automicBrightness;
    }

    public static int getScreenBrightness(Context ctxt) {
        int nowBrightnessValue = 0;
        ContentResolver resolver = ctxt.getContentResolver();
        try {
            nowBrightnessValue = android.provider.Settings.System.getInt(resolver,
                    android.provider.Settings.System.SCREEN_BRIGHTNESS,
                    -1);
        } catch (Exception e) {}
        return nowBrightnessValue;
    }

    public static void autoBrightness(Activity activity, boolean auto) {
        if (auto) {
            ContentResolver resolver = activity.getContentResolver();
            Settings.System.putInt(resolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);

            /*
            Uri uri = android.provider.Settings.System.getUriFor("screen_brightness");
            resolver.notifyChange(uri, null);
            */
        } else {
            Settings.System.putInt(activity.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        }

    }

    public static void setBrightness(Activity activity, int brightness) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.screenBrightness = brightness * (1f / 255f);
        activity.getWindow().setAttributes(lp);
    }
}
