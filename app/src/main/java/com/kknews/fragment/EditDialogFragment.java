package com.kknews.fragment;

import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;

import com.example.ryanwang.helloworld.R;
import com.kknews.callback.DialogClickListener;
import com.kknews.util.Utils;
import com.pkmmte.view.CircularImageView;

import java.util.ArrayList;

/**
 * Created by ryanwang on 2014/9/29.
 */
public class EditDialogFragment extends DialogFragment {

	private Button mButtonCancel;
	private Button mButtonOk;
	private EditText mTextInputFileName;
	private DialogClickListener callback;

	private GridView mGridCategoryImg;
	private CategoryAdapter mCategoryAdapter;

	private ArrayList<String> mDataList;

	static EditDialogFragment newInstance(String editText, ArrayList<String> dataList) {
		EditDialogFragment fragment = new EditDialogFragment();

		Bundle args = new Bundle();
		args.putString(Utils.PASS_EDIT_TEXT_KEY, editText);
		args.putStringArrayList(Utils.PASS_THUMB_NAME_KEY, dataList);
		fragment.setArguments(args);

		return fragment;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		getDialog().setTitle(R.string.edit_favorite_file_name);

		View v = inflater.inflate(R.layout.layout_edit_file_dialog, container, false);

		mTextInputFileName = (EditText) v.findViewById(R.id.text_input_file_name);
		mTextInputFileName.setText(getArguments().getString(Utils.PASS_EDIT_TEXT_KEY));

		mDataList = getArguments().getStringArrayList(Utils.PASS_THUMB_NAME_KEY);

		mCategoryAdapter = new CategoryAdapter(getActivity());
		mGridCategoryImg = (GridView) v.findViewById(R.id.gridview_show_category_img);
		mGridCategoryImg.setAdapter(mCategoryAdapter);
		mGridCategoryImg.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (mCategoryAdapter.selectedId == position) {
					mCategoryAdapter.selectedId = -1;
				} else {
					mCategoryAdapter.selectedId = position;
				}
				mCategoryAdapter.notifyDataSetChanged();
			}
		});

		mButtonCancel = (Button) v.findViewById(R.id.button_cancel);
		mButtonCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				callback.onCancelClick();
				dismiss();
			}
		});

		mButtonOk = (Button) v.findViewById(R.id.button_ok);
		mButtonOk.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				callback.onOkClick();
				dismiss();
			}
		});

		return v;
	}

	public void setCallBack(DialogClickListener callback) {
		this.callback = callback;
	}

	public String getFileName() {
		return mTextInputFileName.getText().toString();
	}

	public void setDataList(ArrayList<String> dataList) {
		this.mDataList = dataList;
	}

	public String getThumbName(){
		if (mCategoryAdapter.selectedId == -1) {
			return null;
		}
		return mDataList.get(mCategoryAdapter.selectedId);
	}

	class CategoryAdapter extends BaseAdapter {

		private LayoutInflater inflater;
		private int selectedId = -1;

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
			ViewHolder holder;

			if (convertView == null) {
				holder = new ViewHolder();
				convertView = inflater.inflate(R.layout.layout_category_item, null);
				//holder.textTitle = (TextView) convertView.findViewById(R.id.text_category_name);
				holder.imageThumb = (CircularImageView) convertView.findViewById(R.id.image_thumb);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			if (mDataList != null) {
				//holder.textTitle.setText(mDataList.get(position).getCategory());
				holder.imageThumb.setImageBitmap(Utils.getBitmapFromInternal(getActivity(), mDataList.get(position)));

				if (selectedId == position) {
					holder.imageThumb.setBorderColor(Color.GREEN);
				} else {
					holder.imageThumb.setBorderColor(Color.WHITE);
				}
			}

			holder.position = position;

			return convertView;
		}
	}

	class ViewHolder {
		int position;
		CircularImageView imageThumb;
		//TextView textTitle;
	}
}
