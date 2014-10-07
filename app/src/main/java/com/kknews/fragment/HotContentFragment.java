package com.kknews.fragment;

import android.content.BroadcastReceiver;
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
	private LinearLayout layoutMultiSelectButtonGroup;
	private Button buttonMultiSelectOk;
	private Button buttonMultiSelectCancel;
	private ListView listViewHotContent;
	private HotEntryAdapter adapterHotEntry;

	private boolean multiSelectMode = false;

	private String title;

	// data
	private ArrayList<ContentDataObject> dataList;
	//db
	private NewsContentDBHelper dbHelper;
	private SQLiteDatabase db;
	//broadcast
	private UpdateUIReceiver updateUiReceiver;
	//menuItem
	private MenuItem multiSelectMenuItem;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.layout_hot_content, container, false);
		TextView textHotTitle = (TextView) view.findViewById(R.id.text_hot_title);
		Bundle bundle = this.getArguments();
		title = bundle.getString(Def.PASS_TITLE_KEY, null);
		textHotTitle.setText(title);

		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		((MyActivity) getActivity()).setDrawerIndicatorEnable(false);

		layoutMultiSelectButtonGroup = (LinearLayout) view.findViewById(R.id.ll_multi_select_button_group);
		buttonMultiSelectOk = (Button) view.findViewById(R.id.button_multi_select_ok);
		buttonMultiSelectCancel = (Button) view.findViewById(R.id.button_multi_select_cancel);
		buttonMultiSelectOk.setOnClickListener(mClickMultiSelectListener);
		buttonMultiSelectCancel.setOnClickListener(mClickMultiSelectListener);

		listViewHotContent = (ListView) view.findViewById(R.id.listview_hot_content);
		listViewHotContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (multiSelectMode) {

					Integer pos = new Integer(position);
					if (adapterHotEntry.getSelectIds().contains(pos)) {
						adapterHotEntry.getSelectIds().remove(pos);
					} else {
						adapterHotEntry.getSelectIds().add(pos);
					}
					multiSelectMenuItem.setTitle(getString(R.string.select)+adapterHotEntry.getSelectIds().size());
					adapterHotEntry.notifyDataSetChanged();

				} else {
					Integer pos = new Integer(position);
					if (adapterHotEntry.getOpenDescription().contains(pos)) {
						adapterHotEntry.getOpenDescription().remove(pos);
					} else {
						adapterHotEntry.getOpenDescription().add(pos);
					}
					adapterHotEntry.notifyDataSetChanged();
				}

			}
		});
		listViewHotContent.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				if (!multiSelectMode) {
					ViewHolder viewHolder = (ViewHolder) view.getTag();
					//showDialog(viewHolder.textTitle.getText().toString(), position);
					showDialog(title, position);
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
		getActivity().registerReceiver(updateUiReceiver, filter);
	}

	@Override
	public void onStart() {
		super.onStart();
		dataList = dbHelper.parseKknewsContentData(dbHelper.getDataCursorFormDB(db, title));
		adapterHotEntry = new HotEntryAdapter(getActivity());
		listViewHotContent.setAdapter(adapterHotEntry);
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(updateUiReceiver);
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
	public void onCreate(Bundle savedInstanceState) {

		setHasOptionsMenu(true);

		dbHelper = new NewsContentDBHelper(getActivity());
		db = dbHelper.getWritableDatabase();

		updateUiReceiver = new UpdateUIReceiver();

		super.onCreate(savedInstanceState);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.action_menu, menu);
		if (menu != null) {
			menu.findItem(R.id.action_add_my_favorite).setVisible(true);
			menu.findItem(R.id.action_add_file).setVisible(false);
			menu.findItem(R.id.action_delete_file).setVisible(false);
			menu.findItem(R.id.action_delete_item).setVisible(false);
			multiSelectMenuItem = menu.findItem(R.id.action_multi_select).setVisible(false);
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_add_my_favorite:
				Log.d(TAG, "action_add_my_favorite");
				listViewHotContent.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
				multiSelectMode = true;
				layoutMultiSelectButtonGroup.setVisibility(View.VISIBLE);
				multiSelectMenuItem.setVisible(true);
				break;
			case R.id.action_multi_select:
				View menuItemView = getActivity().findViewById(R.id.action_multi_select); // SAME ID AS MENU ID
				PopupMenu popupMenu = new PopupMenu(getActivity(), menuItemView);
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
									if (!adapterHotEntry.getSelectIds().contains(i)) {
										adapterHotEntry.getSelectIds().add(i);
									}
								}
								multiSelectMenuItem.setTitle(getString(R.string.select)+ size);
								break;
							case R.id.action_select_cancel_all:
								listViewHotContent.clearChoices();
								adapterHotEntry.getSelectIds().clear();
								multiSelectMenuItem.setTitle(getString(R.string.select)+"0");
								break;
							default:
								break;
						}
						adapterHotEntry.notifyDataSetChanged();
						return true;
					}
				});
				break;
			default:
				break;
		}
		return false;
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

			holder.textTitle.setText(dataList.get(i).getTitle());
			holder.textDescription.setMovementMethod(LinkMovementMethod.getInstance());
			holder.textDescription.setText(Html.fromHtml(dataList.get(i).getDescription()));
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

			new LoadImage(i, holder, dataList.get(i).getImgUrl()).execute();

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
					if (db != null) {
						SparseBooleanArray checkItemList;
						checkItemList = listViewHotContent.getCheckedItemPositions().clone();
						showMultiInsertDialog(title, checkItemList);
					}
				case R.id.button_multi_select_cancel:
					multiSelectMode = false;
					layoutMultiSelectButtonGroup.setVisibility(View.GONE);

					listViewHotContent.clearChoices();
					multiSelectMenuItem.setVisible(false);
					adapterHotEntry.getSelectIds().clear();
					adapterHotEntry.notifyDataSetChanged();
					break;
			}
		}
	};

	private void showMultiInsertDialog(String title, final SparseBooleanArray checkItemList) {

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
						dbHelper.insertContentData(dataList, db, i, newFragment.getSelectFileName());
					}
				}

				dbHelper.insertCategoryData(db, newFragment.getSelectFileName(), Utils.encodeBase64(dataList.get(lastCheckPosition)
						.getImgUrl()));

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
				dbHelper.insertContentData(dataList, db, position, newFragment.getSelectFileName());
				dbHelper.insertCategoryData(db, newFragment.getSelectFileName(), Utils.encodeBase64(dataList.get(position).getImgUrl()));
			}
		});
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
			if (bundle.getString(Def.PASS_TITLE_KEY).equals(title)) {
				dataList = dbHelper.parseKknewsContentData(dbHelper.getDataCursorFormDB(db, title));
				adapterHotEntry = new HotEntryAdapter(getActivity());
				listViewHotContent.setAdapter(adapterHotEntry);

			}
		}
	}
}
