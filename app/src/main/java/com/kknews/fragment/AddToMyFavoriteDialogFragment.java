package com.kknews.fragment;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ryanwang.helloworld.R;
import com.kknews.callback.DialogClickListener;
import com.kknews.util.Utils;

/**
 * Created by ryanwang on 2014/9/29.
 */
public class AddToMyFavoriteDialogFragment extends DialogFragment {

	private Button mButtonCancel;
	private Button mButtonOk;
	private DialogClickListener callback;


	static AddToMyFavoriteDialogFragment newInstance(String editText,String title){
		AddToMyFavoriteDialogFragment fragment = new AddToMyFavoriteDialogFragment();

		Bundle args = new Bundle();
		args.putString(Utils.PASS_EDIT_TEXT_KEY, editText);
		args.putString(Utils.PASS_EDIT_TITLE_KEY, title);
		fragment.setArguments(args);

		return fragment;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		getDialog().setTitle(R.string.add_to_favorite);

		View v = inflater.inflate(R.layout.layout_add_to_my_favorite_dialog,container,false);
		TextView textTitle = (TextView) v.findViewById(R.id.text_title);
		textTitle.setText(getArguments().getString(Utils.PASS_EDIT_TITLE_KEY));

		EditText textInputFileName = (EditText) v.findViewById(R.id.text_input_file_name);
		textInputFileName.setText(getArguments().getString(Utils.PASS_EDIT_TEXT_KEY));

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
				Toast.makeText(getActivity(), "加入成功", Toast.LENGTH_SHORT).show();
			}
		});

		return v;
	}

	public void setCallBack(DialogClickListener callback){
		this.callback = callback;
	}

}
