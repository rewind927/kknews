package com.kknews.util;

import android.content.Context;
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
	public static final String RSS_SUB_URL = "http://www.kkbox.com/tw/tc/rss/index.html";
	public static final String PASS_URL_KEY = "PASS_URL_KEY";
	public static final String PASS_TITLE_KEY = "PASS_TITLE_KEY";
	public static final String PASS_EDIT_TEXT_KEY = "PASS_EDIT_TEXT_KEY";
	public static final String PASS_EDIT_TITLE_KEY = "PASS_EDIT_TITLE_KEY";
	public static final String PASS_THUMB_NAME_KEY = "PASS_THUMB_NAME_KEY";

	public static final String HOT_CONTENT_GUID = "guid";
	public static final String HOT_CONTENT_DATE = "gc:date";
	public static final String HOT_CONTENT_DESCRIPTION = "description";
	public static final String HOT_CONTENT_TITLE = "title";
	public static final String HOT_CONTENT_CATEGORY = "category";

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
}
