package com.kknews.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.ryanwang.helloworld.R;

/**
 * Created by ryanwang on 2014/9/25.
 */
public class PersonalFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_collection_object, container, false);
		TextView t = (TextView)view.findViewById(R.id.text);
		t.setText("personal");
		return view;
	}
}
