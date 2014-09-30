package com.kknews.fragment;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.ryanwang.helloworld.R;
import com.kknews.data.ContentDataObject;
import com.kknews.database.NewsContentDBHelper;
import com.kknews.util.Utils;

import java.util.ArrayList;

/**
 * Created by ryanwang on 2014/9/25.
 */
public class PersonalContentFragment extends Fragment {

	public final static String TAG = "PersonalContentFragment";

	//view
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
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		setHasOptionsMenu(true);

		mDbHelper = new NewsContentDBHelper(getActivity());
		mDB = mDbHelper.getWritableDatabase();

		Bundle bundle = this.getArguments();
		mTitle = bundle.getString(Utils.PASS_TITLE_KEY, null);

		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG,"PersonalContentFragment.onCreateView()");
		View view = inflater.inflate(R.layout.layout_hot_content, container, false);
		TextView textHotTitle = (TextView) view.findViewById(R.id.text_hot_title);
		Bundle bundle = this.getArguments();
		mTitle = bundle.getString(Utils.PASS_TITLE_KEY, null);
		textHotTitle.setText(mTitle);

		mListViewHotContent = (ListView) view.findViewById(R.id.listview_hot_content);
		mAdapterHotEntry = new HotEntryAdapter(getActivity());
		mListViewHotContent.setAdapter(mAdapterHotEntry);

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

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onStart() {
		super.onStart();

		parseData(getDataCursorFromDB());
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
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.action_menu, menu);
		if (menu != null) {
			menu.findItem(R.id.action_add_my_favorite).setVisible(false);
			menu.findItem(R.id.action_add_file).setVisible(false);
			menu.findItem(R.id.action_delete_file).setVisible(false);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return true;
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

	private void parseData(Cursor cursor) {
		if (mDataList == null) {
			mDataList = new ArrayList<ContentDataObject>();
		}

		if (cursor == null) {
			return;
		}

		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			ContentDataObject data = new ContentDataObject();
			data.setTitle(cursor.getString(cursor.getColumnIndex(NewsContentDBHelper.COLUMN_TITLE)));
			data.setDate(cursor.getString(cursor.getColumnIndex(NewsContentDBHelper.COLUMN_DATE)));
			data.setLink(cursor.getString(cursor.getColumnIndex(NewsContentDBHelper.COLUMN_URL)));
			data.setDescription(cursor.getString(cursor.getColumnIndex(NewsContentDBHelper.COLUMN_DESCRIPTION)));
			data.setCategory(cursor.getString(cursor.getColumnIndex(NewsContentDBHelper.COLUMN_FILE)));
			data.setImgUrl(cursor.getString(cursor.getColumnIndex(NewsContentDBHelper.COLUMN_THUMBNAIL)));
			Log.d(TAG, "category:" + cursor.getString(cursor.getColumnIndex(NewsContentDBHelper.COLUMN_FILE)));
			Log.d(TAG, "thumbnail:" + cursor.getString(cursor.getColumnIndex(NewsContentDBHelper.COLUMN_THUMBNAIL)));
			cursor.moveToNext();

			mDataList.add(data);
		}
	}

	private Cursor getDataCursorFromDB(){
		Log.d(TAG,"select ..........:"+mTitle);
		Cursor cursor = mDB.rawQuery("SELECT * FROM "+NewsContentDBHelper.TABLE_CONTENT+" WHERE "+NewsContentDBHelper.COLUMN_FILE +" = '"+mTitle+"'", null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Log.d(TAG, "title:" + cursor.getString(cursor.getColumnIndex(NewsContentDBHelper.COLUMN_TITLE)));
			cursor.moveToNext();
		}
		return cursor;
	}
}
