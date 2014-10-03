package com.kknews.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ryanwang.helloworld.R;
import com.kknews.callback.DialogClickListener;
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
 * Created by ryanwang on 2014/9/25.
 */
public class HotContentFragment extends Fragment {

	public final static String TAG = "HotContentFragment";

	//view
	private LinearLayout mLayoutMultiSelectButtonGroup;
	private Button mButtonMultiSelectOk;
	private Button mButtonMultiSelectCancel;
	private ListView mListViewHotContent;
	private HotEntryAdapter mAdapterHotEntry;

	private boolean mMultiSelectMode = false;

	private String mTitle;

	// data
	private ArrayList<ContentDataObject> mDataList;
	//db
	private NewsContentDBHelper mDbHelper;
	private SQLiteDatabase mDB;
	//broadcast
	private UpdateUIReceiver mUpdateUiReceiver;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.layout_hot_content, container, false);
		TextView textHotTitle = (TextView) view.findViewById(R.id.text_hot_title);
		Bundle bundle = this.getArguments();
		mTitle = bundle.getString(Def.PASS_TITLE_KEY, null);
		textHotTitle.setText(mTitle);

		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);

		mLayoutMultiSelectButtonGroup = (LinearLayout) view.findViewById(R.id.ll_multi_select_button_group);
		mButtonMultiSelectOk = (Button) view.findViewById(R.id.button_multi_select_ok);
		mButtonMultiSelectCancel = (Button) view.findViewById(R.id.button_multi_select_cancel);
		mButtonMultiSelectOk.setOnClickListener(mClickMultiSelectListener);
		mButtonMultiSelectCancel.setOnClickListener(mClickMultiSelectListener);

		mListViewHotContent = (ListView) view.findViewById(R.id.listview_hot_content);
		mListViewHotContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (mMultiSelectMode) {

					Integer pos = new Integer(position);
					if (mAdapterHotEntry.getSelectIds().contains(pos)) {
						mAdapterHotEntry.getSelectIds().remove(pos);
					} else {
						mAdapterHotEntry.getSelectIds().add(pos);
					}
					mAdapterHotEntry.notifyDataSetChanged();

				} else {
					Integer pos = new Integer(position);
					if (mAdapterHotEntry.getOpenDescription().contains(pos)) {
						mAdapterHotEntry.getOpenDescription().remove(pos);
					} else {
						mAdapterHotEntry.getOpenDescription().add(pos);
					}
					mAdapterHotEntry.notifyDataSetChanged();
				}

			}
		});
		mListViewHotContent.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				if (!mMultiSelectMode) {
					ViewHolder viewHolder = (ViewHolder) view.getTag();
					showDialog(viewHolder.textTitle.getText().toString(), position);
				}
				return false;
			}
		});
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Def.ACTION_REFRESH_UI);
		getActivity().registerReceiver(mUpdateUiReceiver, filter);
	}

	@Override
	public void onStart() {
		super.onStart();
		mDataList = parseData(getDataCursorFormDB(mTitle));
		mAdapterHotEntry = new HotEntryAdapter(getActivity());
		mListViewHotContent.setAdapter(mAdapterHotEntry);
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(mUpdateUiReceiver);
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		if (mDbHelper != null) {
			mDbHelper.close();
		}
		if (mDB != null) {
			mDB.close();
		}

		super.onDestroy();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		setHasOptionsMenu(true);

		mDbHelper = new NewsContentDBHelper(getActivity());
		mDB = mDbHelper.getWritableDatabase();

		Bundle bundle = this.getArguments();
		final String dataUrl = bundle.getString(Def.PASS_URL_KEY, null);

		mUpdateUiReceiver = new UpdateUIReceiver();

		super.onCreate(savedInstanceState);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.action_menu, menu);
		if (menu != null) {
			menu.findItem(R.id.action_add_my_favorite).setVisible(true);

			menu.findItem(R.id.action_add_file).setVisible(false);
			menu.findItem(R.id.action_delete_file).setVisible(false);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_add_my_favorite:
				Log.d(TAG, "action_add_my_favorite");
				mListViewHotContent.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
				mMultiSelectMode = true;
				mLayoutMultiSelectButtonGroup.setVisibility(View.VISIBLE);
				break;
			default:
				break;
		}
		return true;
	}

	private Cursor getDataCursorFormDB(String title) {
		return mDB.rawQuery("SELECT * FROM " + NewsContentDBHelper.TABLE_KKEWNS_CONTENT + " WHERE " + NewsContentDBHelper.COLUMN_FILE +
				"" +
				" " +
				"= " + "'" + title + "' ORDER BY " + NewsContentDBHelper.COLUMN_ID + ";", null);
	}

	private ArrayList parseData(Cursor cursor) {
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

	private void getData(String url) {
		mDataList = getXml(url);
		mHandler.sendEmptyMessage(3);
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

	class HotEntryAdapter extends BaseAdapter {

		private LayoutInflater inflater;
		private ArrayList<Integer> selectedIds = new ArrayList<Integer>();
		private ArrayList<Integer> openDescription = new ArrayList<Integer>();

		HotEntryAdapter(Context ctx) {
			inflater = LayoutInflater.from(ctx);
		}

		@Override
		public int getCount() {
			if (mDataList == null) {
				return 0;
			}
			return mDataList.size();
		}

		@Override
		public Object getItem(int i) {
			return mDataList.get(i);
		}

		@Override
		public long getItemId(int i) {
			return 0;
		}

		@Override
		public View getView(int i, View convertView, ViewGroup viewGroup) {
			ViewHolder holder = null;

			if (convertView == null) {
				holder = new ViewHolder();
				convertView = inflater.inflate(R.layout.layout_content_item, null);
				holder.viewThumb = (ImageView) convertView.findViewById(R.id.view_thumb);
				holder.textTitle = (TextView) convertView.findViewById(R.id.text_title);
				holder.textDescription = (TextView) convertView.findViewById(R.id.text_description);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.textTitle.setText(mDataList.get(i).getTitle());
			holder.textDescription.setMovementMethod(LinkMovementMethod.getInstance());
			holder.textDescription.setText(Html.fromHtml(mDataList.get(i).getDescription()));
			holder.position = i;

			if (selectedIds.contains(i)) {
				holder.textTitle.setTextColor(Color.RED);
			} else {
				holder.textTitle.setTextColor(Color.BLACK);
			}

			if (openDescription.contains(i)) {
				holder.textDescription.setVisibility(View.VISIBLE);
			} else {
				holder.textDescription.setVisibility(View.GONE);
			}

			new LoadImage(i, holder, mDataList.get(i).getImgUrl()).execute();

			return convertView;
		}

		public ArrayList getSelectIds() {
			return selectedIds;
		}

		public ArrayList getOpenDescription() {return openDescription;}
	}

	class ViewHolder {
		TextView textTitle;
		ImageView viewThumb;
		TextView textDescription;
		int position;
	}

	private View.OnClickListener mClickMultiSelectListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.button_multi_select_ok:
					if (mDB != null) {
						SparseBooleanArray checkItemList;
						checkItemList = mListViewHotContent.getCheckedItemPositions().clone();
						showMutliInsertDialog(mTitle, checkItemList);
					}
				case R.id.button_multi_select_cancel:
					mMultiSelectMode = false;
					mLayoutMultiSelectButtonGroup.setVisibility(View.GONE);

					mListViewHotContent.clearChoices();

					mAdapterHotEntry.getSelectIds().clear();
					mAdapterHotEntry.notifyDataSetChanged();
					break;
			}
		}
	};

	Handler mUIHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Toast.makeText(getActivity(), "get data", Toast.LENGTH_SHORT).show();
			mAdapterHotEntry.notifyDataSetChanged();

		}
	};

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			mAdapterHotEntry = new HotEntryAdapter(getActivity());
			mListViewHotContent.setAdapter(mAdapterHotEntry);

		}
	};

	private void showMutliInsertDialog(String title, final SparseBooleanArray checkItemList) {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment prev = getFragmentManager().findFragmentByTag("dialog");
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);

		final AddToMyFavoriteDialogFragment newFragment = AddToMyFavoriteDialogFragment.newInstance(mTitle, title);

		newFragment.show(this.getActivity().getFragmentManager(), "dialog");
		newFragment.setCallBack(new DialogClickListener() {
			@Override
			public void onCancelClick() {
				if (checkItemList != null) {
					checkItemList.clear();
				}
			}

			@Override
			public void onOkClick() {
				int listSize = mListViewHotContent.getCount();
				int lastCheckPosition = 0;
				for (int i = 0; i < listSize; i++) {
					if (checkItemList.get(i)) {
						lastCheckPosition = i;
						Log.d(TAG, i + ":check ok");
						insertContentData(i, newFragment.getFileName());
					}
				}

				insertCategoryData(newFragment.getFileName(), Utils.encodeBase64(mDataList.get(lastCheckPosition).getImgUrl()));

				if (checkItemList != null) {
					checkItemList.clear();
				}
			}
		});
	}

	private void showDialog(String title, final int position) {

		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment prev = getFragmentManager().findFragmentByTag("dialog");
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);

		final AddToMyFavoriteDialogFragment newFragment = AddToMyFavoriteDialogFragment.newInstance(mTitle, title);

		newFragment.show(this.getActivity().getFragmentManager(), "dialog");
		newFragment.setCallBack(new DialogClickListener() {
			@Override
			public void onCancelClick() {

			}

			@Override
			public void onOkClick() {
				insertContentData(position, newFragment.getFileName());
				insertCategoryData(newFragment.getFileName(), Utils.encodeBase64(mDataList.get(position).getImgUrl()));

			}
		});
	}

	private void insertCategoryData(String fileName, String thumbFileName) {
		ContentValues value = new ContentValues();
		value.put(NewsContentDBHelper.COLUMN_FILE, fileName);
		value.put(NewsContentDBHelper.COLUMN_THUMBNAIL, thumbFileName);
		mDB.insert(NewsContentDBHelper.TABLE_CATEGORY, null, value);
	}

	private void insertContentData(int position, String fileName) {
		ContentDataObject data = mDataList.get(position);
		ContentValues value = new ContentValues();
		value.put(NewsContentDBHelper.COLUMN_FILE, fileName);
		value.put(NewsContentDBHelper.COLUMN_CATEGORY, data.getCategory());
		value.put(NewsContentDBHelper.COLUMN_DATE, data.getDate());
		value.put(NewsContentDBHelper.COLUMN_DESCRIPTION, data.getDescription());
		value.put(NewsContentDBHelper.COLUMN_TITLE, data.getTitle());
		value.put(NewsContentDBHelper.COLUMN_URL, data.getLink());
		value.put(NewsContentDBHelper.COLUMN_THUMBNAIL, Utils.encodeBase64(data.getImgUrl()));
		mDB.insert(NewsContentDBHelper.TABLE_CONTENT, null, value);
	}

	class LoadImage extends AsyncTask<Object, Void, Bitmap> {

		private String mPath;
		private int mPosition;
		private ViewHolder mHolder;
		private String mFileName;

		public LoadImage(int position, ViewHolder holder, String path) {
			this.mPosition = position;
			this.mHolder = holder;
			this.mPath = path;
			this.mFileName = Utils.encodeBase64(mPath);
		}

		@Override
		protected Bitmap doInBackground(Object... params) {
			Bitmap bitmap = null;

			File file = new File(getActivity().getFilesDir(), mFileName);

			Log.d(TAG, "file:" + file.getAbsolutePath());

			if (file.exists()) {
				bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
				return bitmap;
			}

			URL imageURL;
			try {
				imageURL = new URL(mPath);
				bitmap = BitmapFactory.decodeStream(imageURL.openStream());
				Utils.saveImageToInternal(getActivity(), bitmap, mFileName);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			if (mHolder.position == mPosition) {
				if (result != null && mHolder.viewThumb != null) {
					mHolder.viewThumb.setVisibility(View.VISIBLE);
					mHolder.viewThumb.setImageBitmap(result);
				} else {
					mHolder.viewThumb.setVisibility(View.GONE);
				}
			}
		}
	}

	class UpdateUIReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			if (bundle.getString(Def.PASS_TITLE_KEY).equals(mTitle)) {
				mDataList = parseData(getDataCursorFormDB(mTitle));
				mAdapterHotEntry = new HotEntryAdapter(getActivity());
				mListViewHotContent.setAdapter(mAdapterHotEntry);

			}
		}
	}
}
