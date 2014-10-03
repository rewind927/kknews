package com.kknews.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by ryanwang on 2014/9/25.
 */
public class Utils {

	public static String encodeBase64(String data) {
		try {
			return Base64.encodeToString(data.getBytes("UTF-8"), Base64.DEFAULT);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Bitmap getBitmapFromInternal(Context ctx, String fileName) {

		Bitmap bitmap = null;

		if (ctx == null || fileName == null) {
			return null;
		}

		File file = new File(ctx.getFilesDir(), fileName);

		if (file.exists()) {
			bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
		}
		return bitmap;
	}

	public static void saveImageToInternal(Context ctx, Bitmap bmp, String fileName) {
		if (ctx == null) {
			return;
		}
		if (fileName == null) {
			return;
		}
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();

		bmp.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

		File file = new File(ctx.getFilesDir(), fileName);

		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

		FileOutputStream fos = null;
		try {

			fos = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			fos.write(bytes.toByteArray());
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static boolean readAutoRefreshPreference(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(Def.PREFERENCE_AUTO_REFRESH, Context.MODE_PRIVATE);
		return sharedPreferences.getBoolean(Def.PREFERENCE_AUTO_REFRESH_CHECK, false);
	}

	public static void writeAutoRefreshPreference(Context context, boolean isCheck) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(Def.PREFERENCE_AUTO_REFRESH, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean(Def.PREFERENCE_AUTO_REFRESH_CHECK, isCheck);
		editor.commit();
	}

	public static int readAutoRefreshTimePreference(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(Def.PREFERENCE_AUTO_REFRESH, Context.MODE_PRIVATE);
		return sharedPreferences.getInt(Def.PREFERENCE_AUTO_REFRESH_TIME, Def.THIRTY_SECOND);
	}

	public static void writeAutoRefreshTimePreference(Context context, int time) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(Def.PREFERENCE_AUTO_REFRESH, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt(Def.PREFERENCE_AUTO_REFRESH_TIME, time);
		editor.commit();
	}
}
