package com.kknews.service;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.kknews.data.ContentDataObject;
import com.kknews.database.NewsContentDBHelper;
import com.kknews.util.Def;
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

	private ArrayList<String> rssUrls;
	private ArrayList<String> sectionName;

	private ArrayList<ArrayList<ContentDataObject>> dataList;

	//db
	private NewsContentDBHelper dbHelper;
	private SQLiteDatabase db;

	//thread
	private Thread thread;
	private UpdateRunnable runnable;

	@Override
	public void onCreate() {
		super.onCreate();

		dbHelper = new NewsContentDBHelper(getApplicationContext());
		db = dbHelper.getWritableDatabase();

		dataList = new ArrayList<ArrayList<ContentDataObject>>();

		Log.d(TAG, "onCreate() executed");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand() executed");
		if (intent == null) {
			return START_STICKY_COMPATIBILITY;
		}
		if (rssUrls == null) {
			rssUrls = intent.getStringArrayListExtra("url");
		}
		if (sectionName == null) {
			sectionName = intent.getStringArrayListExtra("titles");
		}

		runnable = new UpdateRunnable();
		thread = new Thread(runnable);
		if (!thread.isAlive()) {
			thread.start();
		}

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy() executed");

		if (thread.isAlive()) {
			thread.interrupt();
		}

		if (dbHelper != null) {
			dbHelper.close();
		}
		if (db != null) {
			db.close();
		}
		if (dataList != null) {
			dataList.clear();
		}
		if (rssUrls != null) {
			rssUrls.clear();
		}
		if (sectionName != null) {
			sectionName.clear();
		}

	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
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
					if (subEl.tag().toString().equals(Def.HOT_CONTENT_TITLE)) {
						data.setTitle(subEl.text());
					} else if (subEl.tag().toString().equals(Def.HOT_CONTENT_CATEGORY)) {
						data.setCategory(subEl.text());
					} else if (subEl.tag().toString().equals(Def.HOT_CONTENT_GUID)) {
						data.setLink(subEl.text());
					} else if (subEl.tag().toString().equals(Def.HOT_CONTENT_DATE)) {
						data.setDate(subEl.text());
					} else if (subEl.tag().toString().equals(Def.HOT_CONTENT_DESCRIPTION)) {
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
		for (int i = 0; i < dataList.size(); i++) {
			Log.d(TAG, "i:" + i);
			if (isDataInKknewsTable(dataList.get(i).get(0).getTitle())) {
				continue;
			} else {
				deleteContentInCategory(sectionName.get(i));
			}
			for (int j = 0; j < dataList.get(i).size(); j++) {
				ContentDataObject data = dataList.get(i).get(j);
				ContentValues value = new ContentValues();
				value.put(NewsContentDBHelper.COLUMN_FILE, sectionName.get(i));
				value.put(NewsContentDBHelper.COLUMN_CATEGORY, data.getCategory());
				value.put(NewsContentDBHelper.COLUMN_DATE, data.getDate());
				value.put(NewsContentDBHelper.COLUMN_DESCRIPTION, data.getDescription());
				value.put(NewsContentDBHelper.COLUMN_TITLE, data.getTitle());
				value.put(NewsContentDBHelper.COLUMN_URL, data.getLink());
				value.put(NewsContentDBHelper.COLUMN_THUMBNAIL, data.getImgUrl());
				Log.d(TAG, "insert:" + data.getTitle());
				db.insert(NewsContentDBHelper.TABLE_KKEWNS_CONTENT, null, value);
			}
		}
	}

	private void insertContentData(int position) {
		if (dataList == null || dataList.size() == 0) {
			return;
		}
		if (isDataInKknewsTable(dataList.get(position).get(0).getTitle())) {
			return;
		} else {
			deleteContentInCategory(sectionName.get(position));
		}
		db.beginTransaction();
		for (int j = 0; j < dataList.get(position).size(); j++) {
			ContentDataObject data = dataList.get(position).get(j);
			ContentValues value = new ContentValues();
			value.put(NewsContentDBHelper.COLUMN_FILE, sectionName.get(position));
			value.put(NewsContentDBHelper.COLUMN_CATEGORY, data.getCategory());
			value.put(NewsContentDBHelper.COLUMN_DATE, data.getDate());
			value.put(NewsContentDBHelper.COLUMN_DESCRIPTION, data.getDescription());
			value.put(NewsContentDBHelper.COLUMN_TITLE, data.getTitle());
			value.put(NewsContentDBHelper.COLUMN_URL, data.getLink());
			value.put(NewsContentDBHelper.COLUMN_THUMBNAIL, data.getImgUrl());
			Log.d(TAG, "insert:" + data.getTitle());
			db.insert(NewsContentDBHelper.TABLE_KKEWNS_CONTENT, null, value);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		sendUIRefresh(sectionName.get(position));
	}

	private void sendUIRefresh(String title) {
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putString(Def.PASS_TITLE_KEY, title);
		intent.putExtras(bundle);
		intent.setAction(Def.ACTION_REFRESH_UI);
		sendBroadcast(intent);
	}

	private void deleteContentInCategory(String category) {
		db.delete(NewsContentDBHelper.TABLE_KKEWNS_CONTENT, NewsContentDBHelper.COLUMN_FILE + " = " + "'" + category + "'", null);
	}

	private boolean isDataInKknewsTable(String title) {
		Cursor cursor = db.rawQuery("SELECT " + NewsContentDBHelper.COLUMN_FILE + " FROM " + NewsContentDBHelper.TABLE_KKEWNS_CONTENT +
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
			for (int i = 0; i < rssUrls.size(); i++) {
				dataList.add(getXml(rssUrls.get(i)));
				insertContentData(i);
			}
			long b = System.currentTimeMillis();
			Log.d(TAG, "time:" + (b - a));
			for (int i = 0; i < rssUrls.size(); i++) {
				for (int j = 0; j < dataList.get(i).size(); j++) {
					downloadImage(dataList.get(i).get(j).getImgUrl());
				}
			}
		}
	}
}
