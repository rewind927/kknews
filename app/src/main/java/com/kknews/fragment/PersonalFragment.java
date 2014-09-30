package com.kknews.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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

	@Override
	public void onCreate(Bundle savedInstanceState) {

		setHasOptionsMenu(true);

		super.onCreate(savedInstanceState);
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
}
