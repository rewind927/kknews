package com.kknews.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.ryanwang.helloworld.R;
import com.kknews.activity.MyActivity;
import com.kknews.util.Def;
import com.kknews.util.Utils;

/**
 * Created by ryanwang on 2014/9/25.
 */
public class SettingFragment extends Fragment {

	private String[] mSpinnerText = {"30秒", "1分鐘", "5分鐘", "10分鐘"};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.layout_setting, container, false);
		TextView textTitle = (TextView) view.findViewById(R.id.text_title);
		textTitle.setText(getString(R.string.setting));
		CheckBox checkboxAutoFresh = (CheckBox) view.findViewById(R.id.check_box_auto_refresh);
		checkboxAutoFresh.setChecked(Utils.readAutoRefreshPreference(getActivity()));
		checkboxAutoFresh.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Utils.writeAutoRefreshPreference(getActivity(), isChecked);
			}
		});
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		((MyActivity)getActivity()).setDrawerIndicatorEnable(true);
		Spinner spinnerChoiceTime = (Spinner) view.findViewById(R.id.spinner_choice_time);
		ArrayAdapter spinnerAdapter = new ArrayAdapter<String>(getActivity(),R.layout.layout_spinner, mSpinnerText);
		spinnerAdapter.setDropDownViewResource(R.layout.layout_spinner);
		spinnerChoiceTime.setAdapter(spinnerAdapter);
		spinnerChoiceTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				int time = 0;
				switch (position) {
					case 0:
						time = Def.THIRTY_SECOND;
						break;
					case 1:
						time = Def.ONE_MINUTE;
						break;
					case 2:
						time = Def.FIVE_MINUTE;
						break;
					case 3:
						time = Def.TEN_MINUTE;
						break;
				}
				Utils.writeAutoRefreshTimePreference(getActivity(), time);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
		return view;
	}
}
