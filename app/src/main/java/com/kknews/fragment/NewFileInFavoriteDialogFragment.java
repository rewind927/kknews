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

/**
 * Created by ryanwang on 2014/9/29.
 */
public class NewFileInFavoriteDialogFragment extends DialogFragment {

	private Button mButtonCancel;
	private Button mButtonOk;
	private EditText mTextInputFileName;
	private DialogClickListener callback;


	static NewFileInFavoriteDialogFragment newInstance(){
		NewFileInFavoriteDialogFragment fragment = new NewFileInFavoriteDialogFragment();
		return fragment;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		getDialog().setTitle(R.string.new_file_in_favorite);

		View v = inflater.inflate(R.layout.layout_add_to_my_favorite_dialog,container,false);
		TextView textTitle = (TextView) v.findViewById(R.id.text_title);
		textTitle.setVisibility(View.GONE);

		mTextInputFileName = (EditText) v.findViewById(R.id.text_input_file_name);
		mTextInputFileName.setHint(getString(R.string.new_file_hint));

		mButtonCancel = (Button) v.findViewById(R.id.button_cancel);
		mButtonCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				callback.onCancelClick();
				dismiss();
			}
		});

		mButtonOk = (Button) v.findViewById(R.id.button_ok);
		mButtonOk.setText(getString(R.string.new_file));
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

	public String getFileName(){
		return mTextInputFileName.getText().toString();
	}

}
