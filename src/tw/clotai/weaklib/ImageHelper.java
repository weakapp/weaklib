package tw.clotai.weaklib;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class ImageHelper {

	private final static String TAG = "ImageHelper";
	
	public static BitmapFactory.Options getDefaultOptions() {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inScaled = false;
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		options.inDither = false;
		options.inJustDecodeBounds = false;
		options.inPurgeable = true;
		options.inInputShareable = true;
		options.inTempStorage = new byte[16 * 1024];
		return options;
	}
	
	/** return image width and height **/
	public static boolean decodeImageBounds(final InputStream stream,
			int[] outSize) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(stream, null, options);
		if (options.outHeight > 0 && options.outWidth > 0) {
			outSize[0] = options.outWidth;
			outSize[1] = options.outHeight;
			return true;
		}
		return false;
	}

	public static int computeSampleSize(final int bitmapW, final int bitmapH,
			final int maxW, final int maxH) {

		int sampleSize = 1;

		float est = (Math.max(bitmapW, bitmapH) * 1.0f)
				/ (Math.max(maxW, maxH) * 1.0f);
		sampleSize = Math.round(est);
		return sampleSize;
	}
	
	public static Bitmap decodeBitmap(InputStream in) {
		BitmapFactory.Options options = ImageHelper.getDefaultOptions();
		
		Bitmap bitmap = BitmapFactory.decodeStream(in, null, options);
		
		return bitmap;
	}
	
	public static Bitmap decodeBitmap(String imageFileStr, 
									BitmapFactory.Options options, 
									int[] reso) {
		Bitmap ret = null;
		
		Bitmap bitmap = null;
		Bitmap newBitmap = null;
		InputStream in = null;
		
		int maxW = reso[0];
		int maxH = reso[1];
		
		File file = new File(imageFileStr);
		if (!file.exists() || !file.canRead()) {
			Log.e(TAG, "Image not exist or can't read.["+file.getAbsolutePath()+"]");
			return null;
		}
		
		try {
			try {
				in = new FileInputStream(file);
				bitmap = BitmapFactory.decodeStream(in, null, options);
			} finally {
				if (in != null) {
					in.close();
					in = null;
				}
			}
			
			if (bitmap == null) {
				Log.e(TAG, "Decode image failed.");
				/** if decode this image failed, assume this file is corrupted. **/
				file.delete();
				return null;
			}

			float scale = 0;
			int newWidth = 0;
			int newHeight = 0;

			scale = (Math.max(bitmap.getWidth(), bitmap.getHeight()) * 1.0f)
					/ (Math.max(maxW, maxH) * 1.0f);

			if (scale > 1) {
				newWidth = (int) (bitmap.getWidth() / scale);
				newHeight = (int) (bitmap.getHeight() / scale);

				if ((newWidth != 0) && (newHeight != 0)) {
					newBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
					if (newBitmap != bitmap) {
						bitmap.recycle();
						bitmap = null;
					}

				} else {
					newBitmap = bitmap;
				}
			} else {
				newBitmap = bitmap;
			}
			bitmap = null;
			ret = newBitmap;

		} catch (OutOfMemoryError error) {
			if (null != bitmap) bitmap.recycle();
			if (null != newBitmap) newBitmap.recycle();
			if (ret != null) ret.recycle();
			
			ret = null;
			newBitmap = null;
			bitmap = null;
			System.gc();

		} catch (IOException e) {
			Log.e(TAG, "IOException: " + e.getMessage());
			e.printStackTrace();
		}
		return ret;
	}



}
