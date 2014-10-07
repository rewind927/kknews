package com.kknews.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.kknews.data.ContentDataObject;
import com.kknews.util.Utils;

import java.util.ArrayList;

/**
 * Created by ryanwang on 2014/9/30.
 */
public class NewsContentDBHelper extends SQLiteOpenHelper {

	public static final String TAG = "NewsContentDBHelper";

	private static final String DATABASE_NAME = "news_content.db";
	public static final String TABLE_CONTENT = "content";
	public static final String TABLE_CATEGORY = "category";
	public static final String TABLE_KKEWNS_CONTENT = "kknews_content";
	private static final int DATABASE_VERSION = 1;

	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_FILE = "file";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_DESCRIPTION = "description";
	public static final String COLUMN_THUMBNAIL = "thumbnail";
	public static final String COLUMN_URL = "url";
	public static final String COLUMN_DATE = "date";
	public static final String COLUMN_CATEGORY = "category";

	private static final String SQL_CREATE_KKNEWS_CONTENT_TABLE = "CREATE TABLE " + TABLE_KKEWNS_CONTENT + "(" + COLUMN_ID + " integer " +
			"primary key " +
			"autoincrement, " + COLUMN_FILE + " text, " + COLUMN_TITLE + " text, " +
			COLUMN_DESCRIPTION + " text, " + COLUMN_THUMBNAIL + " text, " + COLUMN_URL + " text, " + COLUMN_DATE + " text, " +
			"" + COLUMN_CATEGORY + " text , UNIQUE ( " + COLUMN_FILE + "," + COLUMN_TITLE + " ));";
	private static final String SQL_CREATE_CONTENT_TABLE = "CREATE TABLE " + TABLE_CONTENT + "(" + COLUMN_ID + " integer primary key " +
			"autoincrement, " + COLUMN_FILE + " text, " + COLUMN_TITLE + " text, " +
			COLUMN_DESCRIPTION + " text, " + COLUMN_THUMBNAIL + " text, " + COLUMN_URL + " text, " + COLUMN_DATE + " text, " +
			"" + COLUMN_CATEGORY + " text , UNIQUE ( " + COLUMN_FILE + "," + COLUMN_TITLE + " ));";
	private static final String SQL_CREATE_CATEGORY_TABLE = "CREATE TABLE " + TABLE_CATEGORY + "(" + COLUMN_ID + " integer primary key " +
			"autoincrement, " + COLUMN_FILE + " text UNIQUE, " + COLUMN_THUMBNAIL + " text );";
	public static final String SQL_SELECT_CATEGORY_DATA = "SELECT * FROM " + TABLE_CATEGORY + ";";

	private static final String SQL_DROP_CONTENT_TABLE = "DROP TABLE IF EXISTS " + TABLE_CONTENT;
	private static final String SQL_DROP_CATEGORY_TABLE = "DROP TABLE IF EXISTS " + TABLE_CATEGORY;
	private static final String SQL_DROP_KKNEWS_CONTENT_TABLE = "DROP TABLE IF EXISTS " + TABLE_KKEWNS_CONTENT;

	public NewsContentDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_CONTENT_TABLE);
		db.execSQL(SQL_CREATE_CATEGORY_TABLE);
		db.execSQL(SQL_CREATE_KKNEWS_CONTENT_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(SQL_DROP_CONTENT_TABLE);
		db.execSQL(SQL_DROP_CATEGORY_TABLE);
		db.execSQL(SQL_DROP_KKNEWS_CONTENT_TABLE);
		onCreate(db);
	}

	public static Cursor getCategoryCursorFromDB(SQLiteDatabase db) {
		Cursor cursor = db.rawQuery(NewsContentDBHelper.SQL_SELECT_CATEGORY_DATA, null);
		cursor.moveToFirst();
		return cursor;
	}

	public static ArrayList<String> parseThumbList(Cursor cursor) {
		ArrayList<String> imageList = new ArrayList<String>();
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			String thumbName = cursor.getString(cursor.getColumnIndex(NewsContentDBHelper.COLUMN_THUMBNAIL));
			imageList.add(thumbName);
			cursor.moveToNext();
		}
		return imageList;
	}

	public static ArrayList<String> parseCategoryNameList(Cursor cursor) {
		ArrayList<String> imageList = new ArrayList<String>();
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			String thumbName = cursor.getString(cursor.getColumnIndex(NewsContentDBHelper.COLUMN_FILE));
			imageList.add(thumbName);
			cursor.moveToNext();
		}
		return imageList;
	}

	public void insertContentData(ArrayList<ContentDataObject> dataList, SQLiteDatabase db, int position, String fileName) {
		ContentDataObject data = dataList.get(position);
		ContentValues value = new ContentValues();
		value.put(NewsContentDBHelper.COLUMN_FILE, fileName);
		value.put(NewsContentDBHelper.COLUMN_CATEGORY, data.getCategory());
		value.put(NewsContentDBHelper.COLUMN_DATE, data.getDate());
		value.put(NewsContentDBHelper.COLUMN_DESCRIPTION, data.getDescription());
		value.put(NewsContentDBHelper.COLUMN_TITLE, data.getTitle());
		value.put(NewsContentDBHelper.COLUMN_URL, data.getLink());
		value.put(NewsContentDBHelper.COLUMN_THUMBNAIL, Utils.encodeBase64(data.getImgUrl()));
		db.insert(NewsContentDBHelper.TABLE_CONTENT, null, value);
	}

	public void insertContentDataNoEncode(ArrayList<ContentDataObject> dataList, SQLiteDatabase db, int position, String fileName) {
		ContentDataObject data = dataList.get(position);
		ContentValues value = new ContentValues();
		value.put(NewsContentDBHelper.COLUMN_FILE, fileName);
		value.put(NewsContentDBHelper.COLUMN_CATEGORY, data.getCategory());
		value.put(NewsContentDBHelper.COLUMN_DATE, data.getDate());
		value.put(NewsContentDBHelper.COLUMN_DESCRIPTION, data.getDescription());
		value.put(NewsContentDBHelper.COLUMN_TITLE, data.getTitle());
		value.put(NewsContentDBHelper.COLUMN_URL, data.getLink());
		value.put(NewsContentDBHelper.COLUMN_THUMBNAIL, data.getImgUrl());
		db.insert(NewsContentDBHelper.TABLE_CONTENT, null, value);
	}

	public void insertCategoryData(SQLiteDatabase db, String fileName, String thumbFileName) {
		ContentValues value = new ContentValues();
		value.put(NewsContentDBHelper.COLUMN_FILE, fileName);
		value.put(NewsContentDBHelper.COLUMN_THUMBNAIL, thumbFileName);
		db.insert(NewsContentDBHelper.TABLE_CATEGORY, null, value);
	}

	public Cursor getDataCursorFormDB(SQLiteDatabase db, String title) {
		return db.rawQuery("SELECT * FROM " + NewsContentDBHelper.TABLE_KKEWNS_CONTENT + " WHERE " + NewsContentDBHelper.COLUMN_FILE +
				"" +
				" " +
				"= " + "'" + title + "' ORDER BY " + NewsContentDBHelper.COLUMN_ID + ";", null);
	}

	public ArrayList parseKknewsContentData(Cursor cursor) {
		if (cursor == null) {
			return null;
		}
		ArrayList<ContentDataObject> dataList = null;
		dataList = new ArrayList<ContentDataObject>();
		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			ContentDataObject data = new ContentDataObject();
			data.setTitle(cursor.getString(cursor.getColumnIndex(NewsContentDBHelper.COLUMN_TITLE)));
			data.setCategory(cursor.getString(cursor.getColumnIndex(NewsContentDBHelper.COLUMN_CATEGORY)));
			data.setDate(cursor.getString(cursor.getColumnIndex(NewsContentDBHelper.COLUMN_DATE)));
			data.setLink(cursor.getString(cursor.getColumnIndex(NewsContentDBHelper.COLUMN_URL)));
			data.setImgUrl(cursor.getString(cursor.getColumnIndex(NewsContentDBHelper.COLUMN_THUMBNAIL)));
			data.setDescription(cursor.getString(cursor.getColumnIndex(NewsContentDBHelper.COLUMN_DESCRIPTION)));

			Log.d(TAG, "category:" + cursor.getString(cursor.getColumnIndex(NewsContentDBHelper.COLUMN_FILE)));
			Log.d(TAG, "thumbnail:" + cursor.getString(cursor.getColumnIndex(NewsContentDBHelper.COLUMN_THUMBNAIL)));
			cursor.moveToNext();

			dataList.add(data);
		}
		return dataList;
	}

	public Cursor getCategoryContentCursorFromDB(SQLiteDatabase db, String title) {
		Cursor cursor = db.rawQuery("SELECT " + NewsContentDBHelper.COLUMN_THUMBNAIL + " FROM " + NewsContentDBHelper.TABLE_CONTENT + "" +
				" " +
				"WHERE " + NewsContentDBHelper.COLUMN_FILE + " = '" + title + "'", null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Log.d(TAG, "thumbnail:" + cursor.getString(cursor.getColumnIndex(NewsContentDBHelper.COLUMN_THUMBNAIL)));
			cursor.moveToNext();
		}
		return cursor;
	}

	public Cursor getContentCursorFromDB(SQLiteDatabase db) {
		Cursor cursor = db.rawQuery("SELECT " + NewsContentDBHelper.COLUMN_THUMBNAIL + " FROM " + NewsContentDBHelper
				.TABLE_KKEWNS_CONTENT + "" +
				" ;", null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Log.d(TAG, "thumbnail:" + cursor.getString(cursor.getColumnIndex(NewsContentDBHelper.COLUMN_THUMBNAIL)));
			cursor.moveToNext();
		}
		return cursor;
	}

	public void deleteFavoriteCategory(SQLiteDatabase db, String category) {
		Log.d(TAG, "delete:" + category);
		deleteFavoriteContent(db, category);

		db.delete(NewsContentDBHelper.TABLE_CATEGORY, NewsContentDBHelper.COLUMN_FILE + " = " + "'" + category + "'", null);
	}

	public void deleteFavoriteContent(SQLiteDatabase db, String category) {
		db.delete(NewsContentDBHelper.TABLE_CONTENT, NewsContentDBHelper.COLUMN_FILE + " = " + "'" + category + "'", null);
	}

	public void updateContentCategory(SQLiteDatabase db, String originalTitle, String replaceTitle) {
		ContentValues values = new ContentValues();
		values.put(NewsContentDBHelper.COLUMN_FILE, replaceTitle);
		try {
			db.update(NewsContentDBHelper.TABLE_CONTENT, values, NewsContentDBHelper.COLUMN_FILE + " = " + "'" + originalTitle + "'",
					null);
		} catch (SQLiteConstraintException exception) {
			//deleteContent();
		}
	}

	public void updateCategoryThumb(SQLiteDatabase db, String originalTitle, String replaceTitle, String thumbName) {
		Log.d(TAG, "originalTitle:" + originalTitle + ",replaceTitle:" + replaceTitle + ",thumbName:" + thumbName);

		//update content
		updateContentCategory(db, originalTitle, replaceTitle);

		//update category
		ContentValues values = new ContentValues();
		if (thumbName != null) {
			values.put(NewsContentDBHelper.COLUMN_THUMBNAIL, thumbName);
		}
		values.put(NewsContentDBHelper.COLUMN_FILE, replaceTitle);
		try {
			db.update(NewsContentDBHelper.TABLE_CATEGORY, values, NewsContentDBHelper.COLUMN_FILE + " = " + "'" + originalTitle + "'",
					null);
		} catch (SQLiteConstraintException exception) {
			deleteFavoriteCategory(db, originalTitle);
		}
	}

	public ArrayList<String> parseCategoryThumbList(Cursor cursor, boolean encode) {
		ArrayList<String> imageList = new ArrayList<String>();
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			String thumbName = cursor.getString(cursor.getColumnIndex(NewsContentDBHelper.COLUMN_THUMBNAIL));
			if (encode) {
				thumbName = Utils.encodeBase64(thumbName);
			}
			imageList.add(thumbName);
			cursor.moveToNext();
		}

		return imageList;
	}

	public Cursor getContentDataCursorFromDB(SQLiteDatabase db, String title) {
		Cursor cursor = db.rawQuery("SELECT * FROM " + NewsContentDBHelper.TABLE_CONTENT + " WHERE " + NewsContentDBHelper.COLUMN_FILE +
				" = '" + title + "'", null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Log.d(TAG, "title:" + cursor.getString(cursor.getColumnIndex(NewsContentDBHelper.COLUMN_TITLE)));
			cursor.moveToNext();
		}
		return cursor;
	}
}
