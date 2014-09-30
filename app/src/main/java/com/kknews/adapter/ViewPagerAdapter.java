package com.kknews.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by ryanwang on 2014/9/25.
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {

	private List<Fragment> fragments;

	public ViewPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
		super(fm);
		this.fragments = fragments;
	}

	@Override
	public Fragment getItem(int i) {
		return fragments.get(i);
	}

	@Override
	public int getCount() {
		if (fragments == null) {
			return 0;
		}
		return fragments.size();
	}

	public List getFragments(){
		return fragments;
	}

}
