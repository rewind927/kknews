package com.kknews.service;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.kknews.data.ContentDataObject;
import com.kknews.database.NewsContentDBHelper;
import com.kknews.util.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by ryanwang on 2014/10/2.
 */
public class GetMetaDataService extends Service {

	private static final String TAG = "GetMetaDataService";

	private MyBinder mBinder = new MyBinder();

	private ArrayList<String> mRssUrls;
	private ArrayList<String> mSectionName;

	private ArrayList<ArrayList<ContentDataObject>> mDataList;

	//db
	private NewsContentDBHelper mDbHelper;
	private SQLiteDatabase mDB;

	//thread
	private Thread mThread;
	private UpdateRunnable mRunnable;

	@Override
	public void onCreate() {
		super.onCreate();

		mDbHelper = new NewsContentDBHelper(getApplicationContext());
		mDB = mDbHelper.getWritableDatabase();

		mDataList = new ArrayList<ArrayList<ContentDataObject>>();

		Log.d(TAG, "onCreate() executed");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand() executed");
		if (intent == null) {
			return START_STICKY_COMPATIBILITY;
		}
		if (mRssUrls == null) {
			mRssUrls = intent.getStringArrayListExtra("url");
		}
		if (mSectionName == null) {
			mSectionName = intent.getStringArrayListExtra("titles");
		}

		mRunnable = new UpdateRunnable();
		mThread = new Thread(mRunnable);
		if (!mThread.isAlive()) {
			mThread.start();
		}

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy() executed");

		if (mThread.isAlive()) {
			mThread.interrupt();
		}

		if (mDbHelper != null) {
			mDbHelper.close();
		}
		if (mDB != null) {
			mDB.close();
		}
		if (mDataList != null) {
			mDataList.clear();
		}
		if (mRssUrls != null) {
			mRssUrls.clear();
		}
		if (mSectionName != null) {
			mSectionName.clear();
		}

	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public class MyBinder extends Binder {

		public ArrayList<ArrayList<ContentDataObject>> getData() {
			return mDataList;
		}
	}

	private ArrayList<ContentDataObject> getXml(String url) {
		ArrayList<ContentDataObject> dataList = null;

		try {
			Document doc = Jsoup.connect(url).get();

			Elements metaElements = doc.select("item");
			Log.d(TAG, "metaElements:" + metaElements.size());

			dataList = new ArrayList<ContentDataObject>();

			for (Element el : metaElements) {
				ContentDataObject data = new ContentDataObject();

				for (Element subEl : el.children()) {
					if (subEl.tag().toString().equals(Utils.HOT_CONTENT_TITLE)) {
						data.setTitle(subEl.text());
					} else if (subEl.tag().toString().equals(Utils.HOT_CONTENT_CATEGORY)) {
						data.setCategory(subEl.text());
					} else if (subEl.tag().toString().equals(Utils.HOT_CONTENT_GUID)) {
						data.setLink(subEl.text());
					} else if (subEl.tag().toString().equals(Utils.HOT_CONTENT_DATE)) {
						data.setDate(subEl.text());
					} else if (subEl.tag().toString().equals(Utils.HOT_CONTENT_DESCRIPTION)) {
						String html = subEl.text();
						Document docDescription = Jsoup.parse(html);
						Elements elements = docDescription.select("img");
						data.setImgUrl(elements.get(0).attr("src"));
						//TODO download high resolution picture
						//data.setImgUrl(elements.get(0).attr("src").replaceAll("140x140","200x200"));
						elements = docDescription.select(" 文章內容 ");
						Log.d(TAG, "elements.get(0).text():" + elements.size() + ",");
						String tempString = subEl.text();
						String skipString = "</a>";
						tempString = tempString.substring(tempString.indexOf(skipString) + skipString.length());
						data.setDescription(tempString);
					}
					Log.d(TAG, subEl.tag() + ":" + subEl.text());
				}
				dataList.add(data);
				Log.d(TAG, "------------------------");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return dataList;
	}

	private void downloadImage(String url) {

		String fileName = Utils.encodeBase64(url);
		File file = new File(getApplicationContext().getFilesDir(), fileName);

		if (!file.exists()) {
			URL imageURL;
			try {
				imageURL = new URL(url);
				Bitmap bitmap = BitmapFactory.decodeStream(imageURL.openStream());
				Utils.saveImageToInternal(getApplicationContext(), bitmap, fileName);
				Log.d(TAG, "file:" + file.getAbsolutePath() + "download ok");
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void insertContentData() {
		for (int i = 0; i < mDataList.size(); i++) {
			Log.d(TAG, "i:" + i);
			if (isDataInKknewsTable(mDataList.get(i).get(0).getTitle())) {
				continue;
			} else {
				deleteContentInCategory(mSectionName.get(i));
			}
			for (int j = 0; j < mDataList.get(i).size(); j++) {
				ContentDataObject data = mDataList.get(i).get(j);
				ContentValues value = new ContentValues();
				value.put(NewsContentDBHelper.COLUMN_FILE, mSectionName.get(i));
				value.put(NewsContentDBHelper.COLUMN_CATEGORY, data.getCategory());
				value.put(NewsContentDBHelper.COLUMN_DATE, data.getDate());
				value.put(NewsContentDBHelper.COLUMN_DESCRIPTION, data.getDescription());
				value.put(NewsContentDBHelper.COLUMN_TITLE, data.getTitle());
				value.put(NewsContentDBHelper.COLUMN_URL, data.getLink());
				value.put(NewsContentDBHelper.COLUMN_THUMBNAIL, data.getImgUrl());
				Log.d(TAG, "insert:" + data.getTitle());
				mDB.insert(NewsContentDBHelper.TABLE_KKEWNS_CONTENT, null, value);
			}
		}
	}

	private void insertContentData(int position) {
		if (mDataList == null || mDataList.size() == 0) {
			return;
		}
		if (isDataInKknewsTable(mDataList.get(position).get(0).getTitle())) {
			return;
		} else {
			deleteContentInCategory(mSectionName.get(position));
		}
		mDB.beginTransaction();
		for (int j = 0; j < mDataList.get(position).size(); j++) {
			ContentDataObject data = mDataList.get(position).get(j);
			ContentValues value = new ContentValues();
			value.put(NewsContentDBHelper.COLUMN_FILE, mSectionName.get(position));
			value.put(NewsContentDBHelper.COLUMN_CATEGORY, data.getCategory());
			value.put(NewsContentDBHelper.COLUMN_DATE, data.getDate());
			value.put(NewsContentDBHelper.COLUMN_DESCRIPTION, data.getDescription());
			value.put(NewsContentDBHelper.COLUMN_TITLE, data.getTitle());
			value.put(NewsContentDBHelper.COLUMN_URL, data.getLink());
			value.put(NewsContentDBHelper.COLUMN_THUMBNAIL, data.getImgUrl());
			Log.d(TAG, "insert:" + data.getTitle());
			mDB.insert(NewsContentDBHelper.TABLE_KKEWNS_CONTENT, null, value);
		}
		mDB.setTransactionSuccessful();
		mDB.endTransaction();
		sendUIRefresh(mSectionName.get(position));
	}

	private void sendUIRefresh(String title) {
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putString(Utils.PASS_TITLE_KEY, title);
		intent.putExtras(bundle);
		intent.setAction(Utils.ACTION_REFRESH_UI);
		sendBroadcast(intent);
	}

	private void deleteContentInCategory(String category) {
		mDB.delete(NewsContentDBHelper.TABLE_KKEWNS_CONTENT, NewsContentDBHelper.COLUMN_FILE + " = " + "'" + category + "'", null);
	}

	private boolean isDataInKknewsTable(String title) {
		Cursor cursor = mDB.rawQuery("SELECT " + NewsContentDBHelper.COLUMN_FILE + " FROM " + NewsContentDBHelper.TABLE_KKEWNS_CONTENT +
				"" +
				" " +
				"WHERE " + NewsContentDBHelper.COLUMN_TITLE + " = '" + title + "'", null);

		if (cursor != null && cursor.getCount() > 0) {
			return true;
		}

		return false;
	}

	class UpdateRunnable implements Runnable {

		@Override
		public void run() {
			do {
				getData();

				boolean isAutoUpdate = Utils.readAutoRefreshPreference(getApplicationContext());

				Log.d(TAG, "--------------------------------------check------------------------------------:" + isAutoUpdate);
				if (isAutoUpdate) {
					try {
						int time = Utils.readAutoRefreshTimePreference(getApplicationContext());
						Log.d(TAG, "--------------------------------------time------------------------------------:" + time);
						Thread.sleep(time);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			} while (Utils.readAutoRefreshPreference(getApplicationContext()));
		}

		private void getData() {
			long a = System.currentTimeMillis();
			for (int i = 0; i < mRssUrls.size(); i++) {
				mDataList.add(getXml(mRssUrls.get(i)));
				insertContentData(i);
			}
			long b = System.currentTimeMillis();
			Log.d(TAG, "time:" + (b - a));
			for (int i = 0; i < mRssUrls.size(); i++) {
				for (int j = 0; j < mDataList.get(i).size(); j++) {
					downloadImage(mDataList.get(i).get(j).getImgUrl());
				}
			}
		}
	}
}
