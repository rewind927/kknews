package com.kknews.fragment;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.PopupMenu;
import android.widget.TextView;

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
	private LinearLayout layoutMultiSelectButtonGroup;
	private Button buttonMultiSelectOk;
	private Button buttonMultiSelectCancel;
	private ListView listViewHotContent;
	private HotEntryAdapter hotEntryAdapter;

	private boolean multiSelectMode = false;
	private boolean addMode = false; // true: add , false: delete

	private String title;

	// data
	private ArrayList<ContentDataObject> dataList;
	//db
	private NewsContentDBHelper dbHelper;
	private SQLiteDatabase db;
	//menuItem
	private MenuItem multiSelectMenuItem;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		dbHelper = new NewsContentDBHelper(getActivity());
		db = dbHelper.getWritableDatabase();

		Bundle bundle = this.getArguments();
		title = bundle.getString(Def.PASS_TITLE_KEY, null);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "PersonalContentFragment.onCreateView()");
		View view = inflater.inflate(R.layout.layout_hot_content, container, false);
		TextView textHotTitle = (TextView) view.findViewById(R.id.text_hot_title);
		Bundle bundle = this.getArguments();
		title = bundle.getString(Def.PASS_TITLE_KEY, null);
		textHotTitle.setText(title);

		setHasOptionsMenu(true);
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		((MyActivity) getActivity()).setDrawerIndicatorEnable(false);

		layoutMultiSelectButtonGroup = (LinearLayout) view.findViewById(R.id.ll_multi_select_button_group);
		buttonMultiSelectOk = (Button) view.findViewById(R.id.button_multi_select_ok);
		buttonMultiSelectCancel = (Button) view.findViewById(R.id.button_multi_select_cancel);
		buttonMultiSelectOk.setOnClickListener(mClickMultiSelectListener);
		buttonMultiSelectCancel.setOnClickListener(mClickMultiSelectListener);

		listViewHotContent = (ListView) view.findViewById(R.id.listview_hot_content);
		hotEntryAdapter = new HotEntryAdapter(getActivity());
		listViewHotContent.setAdapter(hotEntryAdapter);

		listViewHotContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (multiSelectMode) {

					Integer pos = new Integer(position);
					if (hotEntryAdapter.getSelectIds().contains(pos)) {
						hotEntryAdapter.getSelectIds().remove(pos);
					} else {
						hotEntryAdapter.getSelectIds().add(pos);
					}
					multiSelectMenuItem.setTitle(getString(R.string.select)+hotEntryAdapter.getSelectIds().size());
					hotEntryAdapter.notifyDataSetChanged();

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
		listViewHotContent.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				if (!multiSelectMode) {
					showInsertDialog(title, position);
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

		parseData(dbHelper.getContentDataCursorFromDB(db, title));
	}

	@Override
	public void onStop() {
		super.onStop();
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

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.action_menu, menu);
		if (menu != null) {
			menu.findItem(R.id.action_add_my_favorite).setVisible(true);
			menu.findItem(R.id.action_add_file).setVisible(false);
			menu.findItem(R.id.action_delete_file).setVisible(false);
			menu.findItem(R.id.action_delete_item).setVisible(true);
			multiSelectMenuItem = menu.findItem(R.id.action_multi_select).setVisible(false);
		}
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_delete_item:
				listViewHotContent.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
				multiSelectMode = true;
				addMode = false;
				layoutMultiSelectButtonGroup.setVisibility(View.VISIBLE);
				buttonMultiSelectOk.setText(getString(R.string.delete));
				multiSelectMenuItem.setVisible(true);
				return true;
			case R.id.action_add_my_favorite:
				listViewHotContent.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
				multiSelectMode = true;
				addMode = true;
				layoutMultiSelectButtonGroup.setVisibility(View.VISIBLE);
				buttonMultiSelectOk.setText(getString(R.string.add));
				multiSelectMenuItem.setVisible(true);
				return true;
			case R.id.action_multi_select:
				Log.d("123","action_multi_select:::::::::::::::::::::::");
				View menuItemView = getActivity().findViewById(R.id.action_multi_select); // SAME ID AS MENU ID
				final PopupMenu popupMenu = new PopupMenu(getActivity(), menuItemView);
				popupMenu.inflate(R.menu.multi_select_menu);
				popupMenu.show();
				popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						switch (item.getItemId()) {
							case R.id.action_select_all:
								int size = listViewHotContent.getCount();
								for(int i =0;i<size;i++){
									listViewHotContent.setItemChecked(i,true);
									if (!hotEntryAdapter.getSelectIds().contains(i)) {
										hotEntryAdapter.getSelectIds().add(i);
									}
								}
								multiSelectMenuItem.setTitle(getString(R.string.select)+ size);
								popupMenu.dismiss();
								break;
							case R.id.action_select_cancel_all:
								listViewHotContent.clearChoices();
								hotEntryAdapter.getSelectIds().clear();
								multiSelectMenuItem.setTitle(getString(R.string.select)+"0");
								popupMenu.dismiss();
								break;
							default:
								break;
						}
						hotEntryAdapter.notifyDataSetChanged();
						return false;
					}
				});
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
			if (dataList == null) {
				return 0;
			}
			return dataList.size();
		}

		@Override
		public Object getItem(int i) {
			return dataList.get(i);
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

			if (dataList != null) {
				holder.textTitle.setText(dataList.get(i).getTitle());
				holder.textDescription.setMovementMethod(LinkMovementMethod.getInstance());
				holder.textDescription.setText(Html.fromHtml(dataList.get(i).getDescription()));
				Bitmap bitmap = Utils.getBitmapFromInternal(getActivity(), dataList.get(i).getImgUrl());
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
		if (dataList == null) {
			dataList = new ArrayList<ContentDataObject>();
		}

		dataList.clear();

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

			dataList.add(data);
		}
	}

	private View.OnClickListener mClickMultiSelectListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.button_multi_select_ok:
					if (db != null) {
						SparseBooleanArray checkItemList;
						checkItemList = listViewHotContent.getCheckedItemPositions().clone();
						if (addMode) {
							showMutliInsertDialog(title, checkItemList);
						} else {
							showDeleteContentDialog(checkItemList);
						}
					}
				case R.id.button_multi_select_cancel:
					multiSelectMode = false;
					layoutMultiSelectButtonGroup.setVisibility(View.GONE);

					listViewHotContent.clearChoices();
					multiSelectMenuItem.setVisible(false);
					hotEntryAdapter.getSelectIds().clear();
					hotEntryAdapter.notifyDataSetChanged();
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
				layoutMultiSelectButtonGroup.setVisibility(View.GONE);
				checkList.clear();
			}
		});
		exitAlertDialog.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				layoutMultiSelectButtonGroup.setVisibility(View.GONE);
				deleteSelectContent(checkList);
				parseData(dbHelper.getContentDataCursorFromDB(db, title));
				hotEntryAdapter.notifyDataSetChanged();
				checkList.clear();
			}
		});
		exitAlertDialog.show();
	}

	private void deleteSelectContent(SparseBooleanArray checkItemList) {
		int listSize = listViewHotContent.getCount();
		for (int i = 0; i < listSize; i++) {
			if (checkItemList.get(i)) {
				db.delete(NewsContentDBHelper.TABLE_CONTENT, NewsContentDBHelper.COLUMN_TITLE + " = " + "'" + dataList.get(i).getTitle()
						+ "' AND " + NewsContentDBHelper.COLUMN_FILE + " = " + "'" + title + "'", null);
			}
		}
	}

	private void showInsertDialog(String title, final int position) {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment prev = getFragmentManager().findFragmentByTag("dialog");
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);

		Cursor cursor = NewsContentDBHelper.getCategoryCursorFromDB(db);

		ArrayList<String> imageList = NewsContentDBHelper.parseThumbList(cursor);
		ArrayList<String> titleList = NewsContentDBHelper.parseCategoryNameList(cursor);

		final EditDialogFragment newFragment = EditDialogFragment.newInstance(EditDialogFragment.ADD_MODE, title, imageList, titleList);

		newFragment.show(this.getActivity().getFragmentManager(), "dialog");

		newFragment.setCallBack(new DialogClickListener() {
			@Override
			public void onCancelClick() {

			}

			@Override
			public void onOkClick() {
				dbHelper.insertContentDataNoEncode(dataList, db, position, newFragment.getSelectFileName());
				dbHelper.insertCategoryData(db, newFragment.getSelectFileName(), dataList.get(position).getImgUrl());

				sendCategoryUIRefresh();
			}
		});
	}

	private void showMutliInsertDialog(String title, final SparseBooleanArray checkItemList) {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment prev = getFragmentManager().findFragmentByTag("dialog");
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);

		Cursor cursor = NewsContentDBHelper.getCategoryCursorFromDB(db);

		ArrayList<String> imageList = NewsContentDBHelper.parseThumbList(cursor);
		ArrayList<String> titleList = NewsContentDBHelper.parseCategoryNameList(cursor);

		final EditDialogFragment newFragment = EditDialogFragment.newInstance(EditDialogFragment.ADD_MODE, title, imageList, titleList);

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
				int listSize = listViewHotContent.getCount();
				int lastCheckPosition = 0;
				for (int i = 0; i < listSize; i++) {
					if (checkItemList.get(i)) {
						lastCheckPosition = i;
						Log.d(TAG, i + ":check ok");
						dbHelper.insertContentDataNoEncode(dataList, db, i, newFragment.getSelectFileName());
					}
				}

				dbHelper.insertCategoryData(db, newFragment.getSelectFileName(), dataList.get(lastCheckPosition).getImgUrl());

				sendCategoryUIRefresh();

				if (checkItemList != null) {
					checkItemList.clear();
				}
			}
		});
	}

	private void sendCategoryUIRefresh() {
		Intent intent = new Intent();
		intent.setAction(Def.ACTION_REFRESH_UI);
		getActivity().sendBroadcast(intent);
	}
}
