package com.kknews.fragment;

import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Bitmap;
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
import android.widget.Toast;

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

	public static final int EDIT_MODE = 0;
	public static final int ADD_MODE = 1;
	public static final int NEW_MODE = 2;

	private Button buttonCancel;
	private Button buttonOk;
	private EditText textInputFileName;
	private DialogClickListener callback;
	private TextView textInputTitle;
	private TextView textFileTitle;

	private GridView gridCategoryImg;
	private CategoryAdapter categoryAdapter;

	private ArrayList<String> thumbDataList;
	private ArrayList<String> categoryTitleDataList;

	private int type = EDIT_MODE;

	static EditDialogFragment newInstance(int type, String editText, ArrayList<String> dataList) {
		EditDialogFragment fragment = new EditDialogFragment();

		Bundle args = new Bundle();
		args.putString(Def.PASS_EDIT_TEXT_KEY, editText);
		args.putStringArrayList(Def.PASS_THUMB_NAME_KEY, dataList);
		args.putInt(Def.PASS_DIALOG_TYPE, type);
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

		textInputFileName = (EditText) v.findViewById(R.id.text_input_file_name);
		textInputFileName.setText(getArguments().getString(Def.PASS_EDIT_TEXT_KEY));

		thumbDataList = getArguments().getStringArrayList(Def.PASS_THUMB_NAME_KEY);
		categoryTitleDataList = getArguments().getStringArrayList(Def.PASS_CATEGORY_TITLE_KEY);

		textInputTitle = (TextView) v.findViewById(R.id.text_input_title);
		textFileTitle = (TextView) v.findViewById(R.id.text_change_img_title);

		type = getArguments().getInt(Def.PASS_DIALOG_TYPE);

		categoryAdapter = new CategoryAdapter(getActivity());
		gridCategoryImg = (GridView) v.findViewById(R.id.gridview_show_category_img);
		gridCategoryImg.setAdapter(categoryAdapter);
		gridCategoryImg.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (categoryAdapter.selectedId == position) {
					categoryAdapter.selectedId = -1;
				} else {
					categoryAdapter.selectedId = position;
				}
				categoryAdapter.notifyDataSetChanged();
			}
		});

		buttonCancel = (Button) v.findViewById(R.id.button_cancel);
		buttonCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				callback.onCancelClick();
				dismiss();
			}
		});

		buttonOk = (Button) v.findViewById(R.id.button_ok);
		buttonOk.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (type == NEW_MODE) {
					if (categoryAdapter.selectedId == -1) {
						Toast.makeText(getActivity(), getString(R.string.please_select_img), Toast.LENGTH_SHORT).show();
						return;
					}
				}
				callback.onOkClick();
				dismiss();
			}
		});

		setDialogLayoutByType();

		return v;
	}

	public void setCallBack(DialogClickListener callback) {
		this.callback = callback;
	}

	public String getFileName() {
		return textInputFileName.getText().toString();
	}

	public String getSelectFileName() {
		if (categoryAdapter.selectedId == -1) {
			return textInputFileName.getText().toString();
		}
		return categoryTitleDataList.get(categoryAdapter.selectedId);
	}

	public void setDataList(ArrayList<String> dataList) {
		this.thumbDataList = dataList;
	}

	public String getThumbName() {
		if (categoryAdapter.selectedId == -1) {
			return null;
		}
		return thumbDataList.get(categoryAdapter.selectedId);
	}

	class CategoryAdapter extends BaseAdapter {

		private LayoutInflater inflater;
		private int selectedId = -1;

		CategoryAdapter(Context ctx) {
			inflater = LayoutInflater.from(ctx);
		}

		@Override
		public int getCount() {
			if (thumbDataList != null) {
				return thumbDataList.size();
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
				if (categoryTitleDataList == null) {
					holder.textTitle.setVisibility(View.GONE);
				}

				holder.imageThumb = (CircularImageView) convertView.findViewById(R.id.image_thumb);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			if (categoryTitleDataList != null) {
				holder.textTitle.setText(categoryTitleDataList.get(position));
			}

			if (thumbDataList != null) {
				Bitmap bitmap = Utils.getBitmapFromInternal(getActivity(), thumbDataList.get(position));
				if (bitmap != null) {
					holder.imageThumb.setImageBitmap(bitmap);
				}
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
		int okId = R.string.edit;

		if (type == ADD_MODE) {
			titleId = R.string.add_to_favorite;
			inputTitleId = R.string.add_to_favorite;
			fileTitleId = R.string.choose_file_title;
			okId = R.string.add;
		} else if (type == NEW_MODE) {
			titleId = R.string.new_file_in_favorite;
			inputTitleId = R.string.new_file_hint;
			fileTitleId = R.string.choose_file_img;
			okId = R.string.new_file;
		}
		getDialog().setTitle(titleId);
		textInputTitle.setText(inputTitleId);
		textFileTitle.setText(fileTitleId);
		buttonOk.setText(okId);

	}
}
