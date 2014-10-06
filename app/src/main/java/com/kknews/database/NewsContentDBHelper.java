package com.kknews.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by ryanwang on 2014/9/30.
 */
public class NewsContentDBHelper extends SQLiteOpenHelper {

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

	public static Cursor getCategoryCursorFromDB(SQLiteDatabase mDB) {
		Cursor cursor = mDB.rawQuery("SELECT * FROM " + NewsContentDBHelper.TABLE_CATEGORY + ";", null);
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
}
