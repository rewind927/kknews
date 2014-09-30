package com.kknews.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ryanwang on 2014/9/30.
 */
public class NewsContentDBHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "news_content.db";
	private static final String TABLE_CONTENT = "content";
	private static final int DATABASE_VERSION = 1;

	private static final String COLUMN_ID = "_id";
	private static final String COLUMN_FILE = "file";
	private static final String COLUMN_TITLE = "title";
	private static final String COLUMN_DESCRIPTION = "description";
	private static final String COLUMN_THUMBNAIL = "thumbnail";
	private static final String COLUMN_URL = "url";
	private static final String COLUMN_DATE = "date";
	private static final String COLUMN_CATEGORY = "category";

	private static final String SQL_DATABASE_CREATE = "CREATE TABLE " + TABLE_CONTENT + "(" + COLUMN_ID + " integer primary key " +
			"autoincrement, " + COLUMN_FILE + " text, " + COLUMN_TITLE + " text, " +
			COLUMN_DESCRIPTION + " text, " + COLUMN_THUMBNAIL + " text, " + COLUMN_URL + " text, " + COLUMN_DATE + " text, " +
			"" + COLUMN_CATEGORY + " text );";
	private static final String SQL_DATABASE_DROP = "DROP TABLE IF EXISTS " + TABLE_CONTENT;


	public NewsContentDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(SQL_DATABASE_DROP);
		onCreate(db);
	}
}
