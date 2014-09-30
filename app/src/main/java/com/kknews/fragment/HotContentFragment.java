package com.kknews.fragment;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
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
import com.kknews.util.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.layout_hot_content, container, false);
		TextView textHotTitle = (TextView) view.findViewById(R.id.text_hot_title);
		Bundle bundle = this.getArguments();
		mTitle = bundle.getString(Utils.PASS_TITLE_KEY, null);
		textHotTitle.setText(mTitle);

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
					TextView textDescription = (TextView) view.findViewById(R.id.text_description);
					if (textDescription.getVisibility() == View.GONE) {
						textDescription.setVisibility(View.VISIBLE);
					} else {
						textDescription.setVisibility(View.GONE);
					}
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
	}

	@Override
	public void onStart() {
		super.onStart();
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
		final String dataUrl = bundle.getString(Utils.PASS_URL_KEY, null);

		new Thread() {
			@Override
			public void run() {
				getData(dataUrl);
			}
		}.start();

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

//				int size = mListViewHotContent.getCheckedItemCount();
//				SparseBooleanArray sparseBooleanArray = mListViewHotContent.getCheckedItemPositions();
//				for (int i =0;i<mListViewHotContent.getCount();i++){
//					Log.w("123","i:"+i+","+sparseBooleanArray.get(i));
//				}
//				Log.w("123","size:"+size);
				break;
			default:
				break;
		}
		return true;
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
			holder.viewThumb = (ImageView) convertView.findViewById(R.id.view_thumb);
			holder.position = i;

			if (selectedIds.contains(i)) {
				holder.textTitle.setTextColor(Color.RED);
			} else {
				holder.textTitle.setTextColor(Color.BLACK);
			}

			new LoadImage(i, holder, mDataList.get(i).getImgUrl()).execute();

			return convertView;
		}

		public ArrayList getSelectIds() {
			return selectedIds;
		}
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
						SparseBooleanArray checkItemList = mListViewHotContent.getCheckedItemPositions();
						int listSize = mListViewHotContent.getCount();
						for (int i = 0; i < listSize; i++) {
							if (checkItemList.get(i)) {
								Log.d(TAG, i + ":check ok");
								insertContentData(i, mTitle);
							}
						}
						//TODO
						//add thumbnail path.
						insertCategoryData(mTitle,"");
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

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			mAdapterHotEntry = new HotEntryAdapter(getActivity());
			mListViewHotContent.setAdapter(mAdapterHotEntry);

		}
	};

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
				Toast.makeText(getActivity(), "cancel callback", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onOkClick() {
				Toast.makeText(getActivity(), "onclick callback", Toast.LENGTH_SHORT).show();
				insertContentData(position, newFragment.getFileName());
				insertCategoryData(newFragment.getFileName(),"");

			}
		});
	}

	private void insertCategoryData(String fileName, String thumbUrl) {
		ContentValues value = new ContentValues();
		value.put(NewsContentDBHelper.COLUMN_FILE, fileName);
		value.put(NewsContentDBHelper.COLUMN_THUMBNAIL, thumbUrl);
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
		mDB.insert(NewsContentDBHelper.TABLE_CONTENT, null, value);
	}

	class LoadImage extends AsyncTask<Object, Void, Bitmap> {

		private String mPath;
		private int mPosition;
		private ViewHolder mHolder;

		public LoadImage(int position, ViewHolder holder, String path) {
			this.mPosition = position;
			this.mHolder = holder;
			this.mPath = path;
		}

		@Override
		protected Bitmap doInBackground(Object... params) {
			Bitmap bitmap = null;
//			File file = new File(
//					Environment.getExternalStorageDirectory().getAbsolutePath() + path);
//
//			if(file.exists()){
//				bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
//			}

			URL imageURL = null;
			try {
				imageURL = new URL(mPath);
				bitmap = BitmapFactory.decodeStream(imageURL.openStream());
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

}
