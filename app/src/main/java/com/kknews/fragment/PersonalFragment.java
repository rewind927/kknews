package com.kknews.fragment;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.ryanwang.helloworld.R;
import com.kknews.activity.MyActivity;
import com.kknews.callback.DialogClickListener;
import com.kknews.data.CategoryObject;
import com.kknews.database.NewsContentDBHelper;
import com.kknews.util.Def;
import com.kknews.util.Utils;
import com.pkmmte.view.CircularImageView;

import java.util.ArrayList;

/**
 * Created by ryanwang on 2014/9/25.
 */
public class PersonalFragment extends Fragment {

	private static final String TAG = "PersonalFragment";

	//data
	private ArrayList<CategoryObject> dataList;

	//UI
	private GridView gridViewShowCategory;
	private CategoryAdapter cateGoryAdapter;
	private LinearLayout layoutMultiSelectButtonGroup;
	private Button buttonCancel;
	private Button buttonOk;

	//db
	private NewsContentDBHelper dbHelper;
	private SQLiteDatabase db;

	private boolean multiSelectMode = false;

	private UpdateUIReceiver updateUiReceiver;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.layout_personal, container, false);
		layoutMultiSelectButtonGroup = (LinearLayout) view.findViewById(R.id.ll_multi_select_button_group);
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		((MyActivity) getActivity()).setDrawerIndicatorEnable(true);
		gridViewShowCategory = (GridView) view.findViewById(R.id.gridview_show_category);
		cateGoryAdapter = new CategoryAdapter(getActivity());
		gridViewShowCategory.setAdapter(cateGoryAdapter);
		gridViewShowCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (!multiSelectMode) {
					FragmentManager fragmentManager = getFragmentManager();
					FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
					fragmentTransaction.addToBackStack(null);
					PersonalContentFragment fragment = new PersonalContentFragment();
					Bundle bundle = new Bundle();
					Log.d(TAG, "dataList.get(position).getCategory():" + dataList.get(position).getCategory());
					bundle.putString(Def.PASS_TITLE_KEY, dataList.get(position).getCategory());
					fragment.setArguments(bundle);
					fragmentTransaction.add(R.id.content_frame, fragment);
					fragmentTransaction.commit();
				} else {
					Log.d(TAG, "->> select:" + position);
					Integer pos = new Integer(position);
					if (cateGoryAdapter.getSelectIds().contains(pos)) {
						cateGoryAdapter.getSelectIds().remove(pos);
					} else {
						cateGoryAdapter.getSelectIds().add(pos);
					}
					cateGoryAdapter.notifyDataSetChanged();
				}

			}
		});
		gridViewShowCategory.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				if (!multiSelectMode) {
					ViewHolder viewHolder = (ViewHolder) view.getTag();
					showDialog(viewHolder.textTitle.getText().toString());
				}
				return false;
			}
		});
		buttonCancel = (Button) view.findViewById(R.id.button_multi_select_cancel);
		buttonOk = (Button) view.findViewById(R.id.button_multi_select_ok);
		buttonCancel.setOnClickListener(mClickMultiSelectListener);
		buttonOk.setOnClickListener(mClickMultiSelectListener);
		return view;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		setHasOptionsMenu(true);

		dbHelper = new NewsContentDBHelper(getActivity());
		db = dbHelper.getWritableDatabase();
		getCategoryCursorFromDB();

		updateUiReceiver = new UpdateUIReceiver();

		super.onCreate(savedInstanceState);
	}

	@Override
	public void onStart() {
		Log.d(TAG, "onStart()");
		super.onStart();

		IntentFilter filter = new IntentFilter();
		filter.addAction(Def.ACTION_REFRESH_UI);
		getActivity().registerReceiver(updateUiReceiver, filter);

		parseData(getCategoryCursorFromDB());
		cateGoryAdapter.notifyDataSetChanged();
	}

	@Override
	public void onResume() {
		Log.d(TAG, "onResume()");
		super.onResume();
	}

	@Override
	public void onStop() {
		super.onStop();

		getActivity().unregisterReceiver(updateUiReceiver);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.action_menu, menu);
		if (menu != null) {
			menu.findItem(R.id.action_add_my_favorite).setVisible(false);
			menu.findItem(R.id.action_add_file).setVisible(true);
			menu.findItem(R.id.action_delete_file).setVisible(true);
			menu.findItem(R.id.action_delete_item).setVisible(false);
		}
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_delete_file:
				gridViewShowCategory.clearChoices();
				cateGoryAdapter.getSelectIds().clear();
				gridViewShowCategory.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
				multiSelectMode = true;
				layoutMultiSelectButtonGroup.setVisibility(View.VISIBLE);
				return true;
			case R.id.action_add_file:
				showAddFileDialog();
				return true;
			default:
				break;
		}
		return false;
	}

	@Override
	public void onDestroy() {
		if (dbHelper != null) {
			dbHelper.close();
		}
		if (db != null) {
			db.close();
		}
		super.onDestroy();
	}

	class CategoryAdapter extends BaseAdapter {

		private LayoutInflater inflater;
		private ArrayList<Integer> selectedIds = new ArrayList<Integer>();

		CategoryAdapter(Context ctx) {
			inflater = LayoutInflater.from(ctx);
		}

		@Override
		public int getCount() {
			if (dataList != null) {
				return dataList.size();
			}
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;

			if (convertView == null) {
				holder = new ViewHolder();
				convertView = inflater.inflate(R.layout.layout_category_item, null);
				holder.textTitle = (TextView) convertView.findViewById(R.id.text_category_name);
				holder.imageThumb = (CircularImageView) convertView.findViewById(R.id.image_thumb);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			if (dataList != null) {
				holder.textTitle.setText(dataList.get(position).getCategory());
				Bitmap bitmap = Utils.getBitmapFromInternal(getActivity(), dataList.get(position).getImgUrl());
				if (bitmap != null) {
					holder.imageThumb.setImageBitmap(bitmap);
				} else {
					holder.imageThumb.setImageBitmap(BitmapFactory.decodeResource(getActivity().getResources(),
							R.drawable.ic_launcher));
				}

				if (selectedIds.contains(position)) {
					holder.imageThumb.setBorderColor(Color.GREEN);
				} else {
					holder.imageThumb.setBorderColor(Color.WHITE);
				}
			}

			holder.position = position;

			return convertView;
		}

		public ArrayList getSelectIds() {
			return selectedIds;
		}
	}

	class ViewHolder {
		int position;
		CircularImageView imageThumb;
		TextView textTitle;
	}

	private View.OnClickListener mClickMultiSelectListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.button_multi_select_ok:
					if (db != null) {
						SparseBooleanArray checkItemList = gridViewShowCategory.getCheckedItemPositions();
						int listSize = gridViewShowCategory.getCount();
						for (int i = 0; i < listSize; i++) {
							if (checkItemList.get(i)) {
								Log.d(TAG, i + ":check ok");
								deleteCategory(dataList.get(i).getCategory());
							}
						}
					}
					updateData();
				case R.id.button_multi_select_cancel:
					multiSelectMode = false;
					layoutMultiSelectButtonGroup.setVisibility(View.GONE);

					gridViewShowCategory.clearChoices();

					cateGoryAdapter.getSelectIds().clear();
					cateGoryAdapter.notifyDataSetChanged();
					break;
			}
		}
	};

	public void updateData() {
		parseData(getCategoryCursorFromDB());
	}

	private Cursor getCategoryCursorFromDB() {
		if (db == null) {
			return null;
		}
		Cursor cursor = db.rawQuery(NewsContentDBHelper.SQL_SELECT_CATEGORY_DATA, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Log.d(TAG, "category:" + cursor.getString(cursor.getColumnIndex(NewsContentDBHelper.COLUMN_FILE)));
			Log.d(TAG, "thumbnail:" + cursor.getString(cursor.getColumnIndex(NewsContentDBHelper.COLUMN_THUMBNAIL)));
			cursor.moveToNext();
		}
		return cursor;
	}

	private void parseData(Cursor cursor) {
		if (dataList == null) {
			dataList = new ArrayList<CategoryObject>();
		}

		dataList.clear();

		if (cursor == null) {
			return;
		}

		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			CategoryObject data = new CategoryObject();
			data.setCategory(cursor.getString(cursor.getColumnIndex(NewsContentDBHelper.COLUMN_FILE)));
			data.setImgUrl(cursor.getString(cursor.getColumnIndex(NewsContentDBHelper.COLUMN_THUMBNAIL)));
			Log.d(TAG, "category:" + cursor.getString(cursor.getColumnIndex(NewsContentDBHelper.COLUMN_FILE)));
			Log.d(TAG, "thumbnail:" + cursor.getString(cursor.getColumnIndex(NewsContentDBHelper.COLUMN_THUMBNAIL)));
			cursor.moveToNext();

			dataList.add(data);
		}
	}

	private Cursor getCategoryContentCursorFromDB(String title) {
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

	private Cursor getContentCursorFromDB() {
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

	private ArrayList<String> parseThumbList(Cursor cursor, boolean encode) {
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

	private void showAddFileDialog() {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment prev = getFragmentManager().findFragmentByTag("dialog");
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);

		ArrayList<String> imageList = parseThumbList(getContentCursorFromDB(), true);
		final EditDialogFragment newFragment = EditDialogFragment.newInstance(EditDialogFragment.NEW_MODE, "", imageList);

		newFragment.show(this.getActivity().getFragmentManager(), "dialog");
		newFragment.setCallBack(new DialogClickListener() {
			@Override
			public void onCancelClick() {

			}

			@Override
			public void onOkClick() {
				String fileName = newFragment.getFileName();
				if (fileName == null || fileName.equals("")) {
					return;
				}
				insertCategoryData(fileName, newFragment.getThumbName());
				updateData();
				cateGoryAdapter.notifyDataSetChanged();

			}
		});
	}

	private void showDialog(final String title) {

		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment prev = getFragmentManager().findFragmentByTag("dialog");
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);

		ArrayList<String> imageList = parseThumbList(getCategoryContentCursorFromDB(title), false);
		final EditDialogFragment newFragment = EditDialogFragment.newInstance(EditDialogFragment.EDIT_MODE,title, imageList);

		newFragment.show(this.getActivity().getFragmentManager(), "dialog");
		newFragment.setCallBack(new DialogClickListener() {
			@Override
			public void onCancelClick() {

			}

			@Override
			public void onOkClick() {
				updateCategoryThumb(title, newFragment.getFileName(), newFragment.getThumbName());
				updateData();
				cateGoryAdapter.notifyDataSetChanged();

			}
		});
	}

	private void updateCategoryThumb(String originalTitle, String replaceTitle, String thumbName) {
		Log.d(TAG, "originalTitle:" + originalTitle + ",replaceTitle:" + replaceTitle + ",thumbName:" + thumbName);

		//update content
		updateContentCategory(originalTitle, replaceTitle);

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
			deleteCategory(originalTitle);
		}
	}

	private void updateContentCategory(String originalTitle, String replaceTitle) {
		ContentValues values = new ContentValues();
		values.put(NewsContentDBHelper.COLUMN_FILE, replaceTitle);
		try {
			db.update(NewsContentDBHelper.TABLE_CONTENT, values, NewsContentDBHelper.COLUMN_FILE + " = " + "'" + originalTitle + "'",
					null);
		} catch (SQLiteConstraintException exception) {
			//deleteContent();
		}
	}

	private void deleteCategory(String category) {
		Log.d(TAG, "delete:" + category);
		deleteContent(category);

		db.delete(NewsContentDBHelper.TABLE_CATEGORY, NewsContentDBHelper.COLUMN_FILE + " = " + "'" + category + "'", null);
	}

	private void deleteContent(String category) {
		db.delete(NewsContentDBHelper.TABLE_CONTENT, NewsContentDBHelper.COLUMN_FILE + " = " + "'" + category + "'", null);
	}

	private void insertCategoryData(String fileName, String thumbFileName) {
		ContentValues value = new ContentValues();
		value.put(NewsContentDBHelper.COLUMN_FILE, fileName);
		value.put(NewsContentDBHelper.COLUMN_THUMBNAIL, thumbFileName);
		db.insert(NewsContentDBHelper.TABLE_CATEGORY, null, value);
	}

	class UpdateUIReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			parseData(getCategoryCursorFromDB());
			cateGoryAdapter.notifyDataSetChanged();

		}
	}
}
