package com.kknews.fragment;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ryanwang.helloworld.R;
import com.kknews.data.CategoryObject;
import com.kknews.database.NewsContentDBHelper;
import com.kknews.util.Utils;

import java.util.ArrayList;

/**
 * Created by ryanwang on 2014/9/25.
 */
public class PersonalFragment extends Fragment {

	private static final String TAG = "PersonalFragment";

	//data
	private ArrayList<CategoryObject> mDataList;

	//UI
	private GridView mGridViewShowCategory;
	private CategoryAdapter mCateGoryAdapter;

	//db
	private NewsContentDBHelper mDbHelper;
	private SQLiteDatabase mDB;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG,"onCreateView");
		View view = inflater.inflate(R.layout.layout_personal, container, false);
		mGridViewShowCategory = (GridView) view.findViewById(R.id.gridview_show_category);
		mCateGoryAdapter = new CategoryAdapter(getActivity());
		mGridViewShowCategory.setAdapter(mCateGoryAdapter);
		mGridViewShowCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.d(TAG,"press:"+position);
				Toast.makeText(getActivity(),"position:"+position,Toast.LENGTH_SHORT).show();

				FragmentManager fragmentManager = getFragmentManager();
				FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
				fragmentTransaction.addToBackStack(null);
				PersonalContentFragment fragment = new PersonalContentFragment();
				Bundle bundle = new Bundle();
				Log.d(TAG,"mDataList.get(position).getCategory():"+mDataList.get(position).getCategory());
				bundle.putString(Utils.PASS_TITLE_KEY,mDataList.get(position).getCategory());
				fragment.setArguments(bundle);
				fragmentTransaction.add(R.id.rl_view, fragment);
				fragmentTransaction.commit();
			}
		});
		return view;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		setHasOptionsMenu(true);

		mDbHelper = new NewsContentDBHelper(getActivity());
		mDB = mDbHelper.getWritableDatabase();
		getCategoryCursorFromDB();

		super.onCreate(savedInstanceState);
	}

	@Override
	public void onStart() {
		Log.d(TAG,"onStart()");
		super.onStart();

		parseData(getCategoryCursorFromDB());
		mCateGoryAdapter.notifyDataSetChanged();
	}

	@Override
	public void onResume() {
		Log.d(TAG,"onResume()");
		super.onResume();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.action_menu, menu);
		if (menu != null) {
			menu.findItem(R.id.action_add_my_favorite).setVisible(false);

			menu.findItem(R.id.action_add_file).setVisible(true);
			menu.findItem(R.id.action_delete_file).setVisible(true);
		}
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

	class CategoryAdapter extends BaseAdapter {

		private LayoutInflater inflater;

		CategoryAdapter(Context ctx) {
			inflater = LayoutInflater.from(ctx);
		}

		@Override
		public int getCount() {
			if (mDataList != null) {
				return mDataList.size();
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
			ViewHolder holder = null;

			if (convertView == null) {
				holder = new ViewHolder();
				convertView = inflater.inflate(R.layout.layout_category_item, null);
				holder.textTitle = (TextView) convertView.findViewById(R.id.text_category_name);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			if (mDataList != null) {
				holder.textTitle.setText(mDataList.get(position).getCategory());
			}

			holder.position = position;

			return convertView;
		}
	}

	class ViewHolder {
		int position;
		ImageView imageThumb;
		TextView textTitle;
	}

	public void updateData(){
		parseData(getCategoryCursorFromDB());
	}

	private Cursor getCategoryCursorFromDB() {
		Cursor cursor = mDB.rawQuery(NewsContentDBHelper.SQL_SELECT_CATEGORY_DATA, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Log.d(TAG, "category:" + cursor.getString(cursor.getColumnIndex(NewsContentDBHelper.COLUMN_FILE)));
			Log.d(TAG, "thumbnail:" + cursor.getString(cursor.getColumnIndex(NewsContentDBHelper.COLUMN_THUMBNAIL)));
			cursor.moveToNext();
		}
		return cursor;
	}

	private void parseData(Cursor cursor) {
		if (mDataList == null) {
			mDataList = new ArrayList<CategoryObject>();
		}

		mDataList.clear();

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

			mDataList.add(data);
		}
	}
}
