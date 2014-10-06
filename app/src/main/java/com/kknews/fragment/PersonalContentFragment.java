package com.kknews.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
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
import com.kknews.activity.MyActivity;
import com.kknews.callback.DialogClickListener;
import com.kknews.data.ContentDataObject;
import com.kknews.database.NewsContentDBHelper;
import com.kknews.util.Def;
import com.kknews.util.Utils;

import java.util.ArrayList;

/**
 * Created by ryanwang on 2014/9/25.
 */
public class PersonalContentFragment extends Fragment {

	public final static String TAG = "PersonalContentFragment";

	//view
	private LinearLayout mLayoutMultiSelectButtonGroup;
	private Button mButtonMultiSelectOk;
	private Button mButtonMultiSelectCancel;
	private ListView mListViewHotContent;
	private HotEntryAdapter mAdapterHotEntry;

	private boolean mMultiSelectMode = false;
	private boolean mAddMode = false; // true: add , false: delete

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
		super.onCreate(savedInstanceState);

		mDbHelper = new NewsContentDBHelper(getActivity());
		mDB = mDbHelper.getWritableDatabase();

		Bundle bundle = this.getArguments();
		mTitle = bundle.getString(Def.PASS_TITLE_KEY, null);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "PersonalContentFragment.onCreateView()");
		View view = inflater.inflate(R.layout.layout_hot_content, container, false);
		TextView textHotTitle = (TextView) view.findViewById(R.id.text_hot_title);
		Bundle bundle = this.getArguments();
		mTitle = bundle.getString(Def.PASS_TITLE_KEY, null);
		textHotTitle.setText(mTitle);

		setHasOptionsMenu(true);
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		((MyActivity)getActivity()).setDrawerIndicatorEnable(false);

		mLayoutMultiSelectButtonGroup = (LinearLayout) view.findViewById(R.id.ll_multi_select_button_group);
		mButtonMultiSelectOk = (Button) view.findViewById(R.id.button_multi_select_ok);
		mButtonMultiSelectCancel = (Button) view.findViewById(R.id.button_multi_select_cancel);
		mButtonMultiSelectOk.setOnClickListener(mClickMultiSelectListener);
		mButtonMultiSelectCancel.setOnClickListener(mClickMultiSelectListener);

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
		menu.clear();
		inflater.inflate(R.menu.action_menu, menu);
		if (menu != null) {
			menu.findItem(R.id.action_add_my_favorite).setVisible(true);
			menu.findItem(R.id.action_add_file).setVisible(false);
			menu.findItem(R.id.action_delete_file).setVisible(false);
			menu.findItem(R.id.action_delete_item).setVisible(true);
		}
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_delete_item:
				mListViewHotContent.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
				mMultiSelectMode = true;
				mAddMode = false;
				mLayoutMultiSelectButtonGroup.setVisibility(View.VISIBLE);
				mButtonMultiSelectOk.setText(getString(R.string.delete));
				return true;
			case R.id.action_add_my_favorite:
				mListViewHotContent.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
				mMultiSelectMode = true;
				mAddMode = true;
				mLayoutMultiSelectButtonGroup.setVisibility(View.VISIBLE);
				mButtonMultiSelectOk.setText(getString(R.string.add));
				return true;
			default:
				break;
		}
		return false;
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

			if (mDataList != null) {
				holder.textTitle.setText(mDataList.get(i).getTitle());
				holder.textDescription.setMovementMethod(LinkMovementMethod.getInstance());
				holder.textDescription.setText(Html.fromHtml(mDataList.get(i).getDescription()));
				Bitmap bitmap = Utils.getBitmapFromInternal(getActivity(), mDataList.get(i).getImgUrl());
				if (bitmap != null) {
					holder.viewThumb.setImageBitmap(bitmap);
				} else {
					holder.viewThumb.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
				}
			}

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

		mDataList.clear();

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
			cursor.moveToNext();

			mDataList.add(data);
		}
	}

	private Cursor getDataCursorFromDB() {
		Cursor cursor = mDB.rawQuery("SELECT * FROM " + NewsContentDBHelper.TABLE_CONTENT + " WHERE " + NewsContentDBHelper.COLUMN_FILE +
				" = '" + mTitle + "'", null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Log.d(TAG, "title:" + cursor.getString(cursor.getColumnIndex(NewsContentDBHelper.COLUMN_TITLE)));
			cursor.moveToNext();
		}
		return cursor;
	}

	private View.OnClickListener mClickMultiSelectListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.button_multi_select_ok:
					if (mDB != null) {
						SparseBooleanArray checkItemList;
						checkItemList = mListViewHotContent.getCheckedItemPositions().clone();
						if (mAddMode) {
							showMutliInsertDialog(mTitle, checkItemList);
						} else {
							showDeleteContentDialog(checkItemList);
						}
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

	private void showDeleteContentDialog(final SparseBooleanArray checkList) {
		AlertDialog.Builder exitAlertDialog = new AlertDialog.Builder(getActivity());
		exitAlertDialog.setTitle(getString(R.string.action_delete_my_favorite));
		exitAlertDialog.setMessage(getString(R.string.action_delete_my_favorite));
		exitAlertDialog.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mLayoutMultiSelectButtonGroup.setVisibility(View.GONE);
				checkList.clear();
			}
		});
		exitAlertDialog.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Toast.makeText(getActivity(), "delete", Toast.LENGTH_SHORT).show();
				mLayoutMultiSelectButtonGroup.setVisibility(View.GONE);
				deleteSelectContent(checkList);
				parseData(getDataCursorFromDB());
				mAdapterHotEntry.notifyDataSetChanged();
				checkList.clear();
			}
		});
		exitAlertDialog.show();
	}

	private void deleteSelectContent(SparseBooleanArray checkItemList) {
		int listSize = mListViewHotContent.getCount();
		for (int i = 0; i < listSize; i++) {
			if (checkItemList.get(i)) {
				mDB.delete(NewsContentDBHelper.TABLE_CONTENT, NewsContentDBHelper.COLUMN_TITLE + " = " + "'" + mDataList.get(i).getTitle()
						+ "' AND " + NewsContentDBHelper.COLUMN_FILE + " = " + "'" + mTitle + "'", null);
			}
		}
	}

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
						sendCategoryUIRefresh();
					}
				}

				insertCategoryData(newFragment.getFileName(), mDataList.get(lastCheckPosition).getImgUrl());

				if (checkItemList != null) {
					checkItemList.clear();
				}
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
		value.put(NewsContentDBHelper.COLUMN_THUMBNAIL, data.getImgUrl());
		mDB.insert(NewsContentDBHelper.TABLE_CONTENT, null, value);
	}

	private void sendCategoryUIRefresh( ) {
		Intent intent = new Intent();
		intent.setAction(Def.ACTION_REFRESH_UI);
		getActivity().sendBroadcast(intent);
	}
}
