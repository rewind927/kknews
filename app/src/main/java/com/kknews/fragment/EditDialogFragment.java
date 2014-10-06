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
import android.widget.TextView;

import com.example.ryanwang.helloworld.R;
import com.kknews.callback.DialogClickListener;
import com.kknews.util.Def;
import com.kknews.util.Utils;
import com.pkmmte.view.CircularImageView;

import java.util.ArrayList;

/**
 * Created by ryanwang on 2014/9/29.
 */
public class EditDialogFragment extends DialogFragment {

	public static int EDIT_MODE = 0;
	public static int ADD_MODE = 1;

	private Button mButtonCancel;
	private Button mButtonOk;
	private EditText mTextInputFileName;
	private DialogClickListener callback;
	private TextView mTextInputTitle;
	private TextView mTextFileTitle;

	private GridView mGridCategoryImg;
	private CategoryAdapter mCategoryAdapter;

	private ArrayList<String> mThumbDataList;
	private ArrayList<String> mCategoryTitleDataList;

	private int mType = EDIT_MODE;

	static EditDialogFragment newInstance(String editText, ArrayList<String> dataList) {
		EditDialogFragment fragment = new EditDialogFragment();

		Bundle args = new Bundle();
		args.putString(Def.PASS_EDIT_TEXT_KEY, editText);
		args.putStringArrayList(Def.PASS_THUMB_NAME_KEY, dataList);
		fragment.setArguments(args);

		return fragment;
	}

	static EditDialogFragment newInstance(int type, String editText, ArrayList<String> thumbList,
	                                      ArrayList<String> categoryTitleDataList) {
		EditDialogFragment fragment = new EditDialogFragment();

		Bundle args = new Bundle();
		args.putString(Def.PASS_EDIT_TEXT_KEY, editText);
		args.putStringArrayList(Def.PASS_THUMB_NAME_KEY, thumbList);
		args.putStringArrayList(Def.PASS_CATEGORY_TITLE_KEY, categoryTitleDataList);
		args.putInt(Def.PASS_DIALOG_TYPE, type);
		fragment.setArguments(args);

		return fragment;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.layout_edit_file_dialog, container, false);

		mTextInputFileName = (EditText) v.findViewById(R.id.text_input_file_name);
		mTextInputFileName.setText(getArguments().getString(Def.PASS_EDIT_TEXT_KEY));

		mThumbDataList = getArguments().getStringArrayList(Def.PASS_THUMB_NAME_KEY);
		mCategoryTitleDataList = getArguments().getStringArrayList(Def.PASS_CATEGORY_TITLE_KEY);

		mTextInputTitle = (TextView) v.findViewById(R.id.text_input_title);
		mTextFileTitle = (TextView) v.findViewById(R.id.text_change_img_title);

		mType = getArguments().getInt(Def.PASS_DIALOG_TYPE);

		setDialogLayoutByType();

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

	public String getSelectFileName() {
		if (mCategoryAdapter.selectedId == -1) {
			return mTextInputFileName.getText().toString();
		}
		return mCategoryTitleDataList.get(mCategoryAdapter.selectedId);
	}

	public void setDataList(ArrayList<String> dataList) {
		this.mThumbDataList = dataList;
	}

	public String getThumbName() {
		if (mCategoryAdapter.selectedId == -1) {
			return null;
		}
		return mThumbDataList.get(mCategoryAdapter.selectedId);
	}

	class CategoryAdapter extends BaseAdapter {

		private LayoutInflater inflater;
		private int selectedId = -1;

		CategoryAdapter(Context ctx) {
			inflater = LayoutInflater.from(ctx);
		}

		@Override
		public int getCount() {
			if (mThumbDataList != null) {
				return mThumbDataList.size();
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
				if (mCategoryTitleDataList == null) {
					holder.textTitle.setVisibility(View.GONE);
				}

				holder.imageThumb = (CircularImageView) convertView.findViewById(R.id.image_thumb);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			if (mCategoryTitleDataList != null) {
				holder.textTitle.setText(mCategoryTitleDataList.get(position));
			}

			if (mThumbDataList != null) {
				holder.imageThumb.setImageBitmap(Utils.getBitmapFromInternal(getActivity(), mThumbDataList.get(position)));
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
		TextView textTitle;
	}

	public void setDialogLayoutByType() {
		int titleId = R.string.edit_favorite_file_name;
		int inputTitleId = R.string.edit_favorite_file_name_title;
		int fileTitleId = R.string.edit_favorite_file_image_name_title;

		if (mType == ADD_MODE) {
			titleId = R.string.add_to_favorite;
			inputTitleId = R.string.add_to_favorite;
			fileTitleId = R.string.choose_file_title;
		}
		getDialog().setTitle(titleId);
		mTextInputTitle.setText(inputTitleId);
		mTextFileTitle.setText(fileTitleId);

	}
}
