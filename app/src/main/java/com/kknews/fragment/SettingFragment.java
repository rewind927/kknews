package com.kknews.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.example.ryanwang.helloworld.R;
import com.kknews.service.GetMetaDataService;

/**
 * Created by ryanwang on 2014/9/25.
 */
public class SettingFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.layout_setting, container, false);
		TextView textTitle = (TextView)view.findViewById(R.id.text_title);
		textTitle.setText(getString(R.string.setting));
		CheckBox checkboxAutoFresh = (CheckBox) view.findViewById(R.id.check_box_auto_refresh);
		checkboxAutoFresh.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Intent serviceIntent = new Intent(getActivity(), GetMetaDataService.class);
				if (isChecked){
					getActivity().startService(serviceIntent);
				}else {
					getActivity().stopService(serviceIntent);
				}
			}
		});
		return view;
	}
}
