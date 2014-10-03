package com.kknews.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;

import com.example.ryanwang.helloworld.R;
import com.kknews.adapter.ViewPagerAdapter;
import com.kknews.fragment.HotFragment;
import com.kknews.fragment.PersonalFragment;
import com.kknews.fragment.SettingFragment;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class MyActivity extends FragmentActivity implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {

	private static final int HOT_FRAGMENT_TAG = 0;
	private static final int PERSONAL_FRAGMENT_TAG = 1;
	private static final int SETTING_FRAGMENT_TAG = 2;

	private TabHost mTabHost;
	private ViewPager mViewPager;
	private HashMap<String, TabInfo> mapTabInfo = new HashMap<String, TabInfo>();
	private ViewPagerAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my);

		getActionBar().setHomeButtonEnabled(true);

		initialiseTabHost(savedInstanceState);
		if (savedInstanceState != null) {
			mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab")); //set the tab as per the saved state
		}
		intialiseViewPager();

	}

	@Override
	protected void onResume() {
		super.onResume();
		checkForCrashes();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				getActionBar().setDisplayHomeAsUpEnabled(false);
				FragmentManager fm = getSupportFragmentManager();
				if (fm.getBackStackEntryCount() > 0) {
					fm.popBackStack();
				}
				break;
		}
		return false;
	}

	@Override
	public MenuInflater getMenuInflater() {
		return super.getMenuInflater();
	}

	private class TabInfo {
		private String tag;
		private Class<?> clss;
		private Bundle args;
		private Fragment fragment;

		TabInfo(String tag, Class<?> clazz, Bundle args) {
			this.tag = tag;
			this.clss = clazz;
			this.args = args;
		}
	}

	class TabFactory implements TabContentFactory {

		private final Context mContext;

		public TabFactory(Context context) {
			mContext = context;
		}

		public View createTabContent(String tag) {
			View v = new View(mContext);
			v.setMinimumWidth(0);
			v.setMinimumHeight(0);
			return v;
		}

	}

	private void intialiseViewPager() {
		List<Fragment> fragments = new Vector<Fragment>();
		fragments.add(Fragment.instantiate(this, HotFragment.class.getName()));
		fragments.add(Fragment.instantiate(this, PersonalFragment.class.getName()));
		fragments.add(Fragment.instantiate(this, SettingFragment.class.getName()));
		mAdapter = new ViewPagerAdapter(getSupportFragmentManager(), fragments);
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mAdapter);
		mViewPager.setOnPageChangeListener(this);
	}

	private void initialiseTabHost(Bundle args) {
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup();
		TabInfo tabInfo = null;
		AddTab(this, mTabHost, mTabHost.newTabSpec("tab1").setIndicator(getString(R.string.hot)), (tabInfo = new TabInfo("tab1",
				HotFragment.class, args)));
		mapTabInfo.put(tabInfo.tag, tabInfo);
		AddTab(this, mTabHost, mTabHost.newTabSpec("tab2").setIndicator(getString(R.string.personal)), (tabInfo = new TabInfo("tab2",
				PersonalFragment.class, args)));
		mapTabInfo.put(tabInfo.tag, tabInfo);
		AddTab(this, mTabHost, mTabHost.newTabSpec("tab3").setIndicator(getString(R.string.setting)), (tabInfo = new TabInfo("tab3",
				SettingFragment.class, args)));
		mapTabInfo.put(tabInfo.tag, tabInfo);
		mTabHost.setOnTabChangedListener(this);
	}

	private static void AddTab(MyActivity activity, TabHost tabHost, TabHost.TabSpec tabSpec, TabInfo tabInfo) {
		// Attach a Tab view factory to the spec
		tabSpec.setContent(activity.new TabFactory(activity));
		tabHost.addTab(tabSpec);
	}

	@Override
	public void onPageScrolled(int i, float v, int i2) {

	}

	@Override
	public void onPageSelected(int i) {
		this.mTabHost.setCurrentTab(i);
	}

	@Override
	public void onPageScrollStateChanged(int i) {

	}

	@Override
	public void onTabChanged(String tag) {
		int pos = this.mTabHost.getCurrentTab();
		this.mViewPager.setCurrentItem(pos);

		if (pos == PERSONAL_FRAGMENT_TAG) {
			((PersonalFragment) mAdapter.getFragments().get(pos)).updateData();
		}
	}

	@Override
	public void onBackPressed() {
		getActionBar().setDisplayHomeAsUpEnabled(false);
		FragmentManager fm = getSupportFragmentManager();
		if (fm.getBackStackEntryCount() > 0) {
			super.onBackPressed();
		}else {
			showExitDialog();
		}

	}

	public void setTabHostVisible(int visible) {
		if (mTabHost != null) {
			mTabHost.getTabWidget().setVisibility(visible);
		}
	}

	private void showExitDialog(){
		AlertDialog.Builder exitAlertDialog = new AlertDialog.Builder(this);
		exitAlertDialog.setTitle(getString(R.string.exit));
		exitAlertDialog.setMessage(getString(R.string.exit_description));
		exitAlertDialog.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});
		exitAlertDialog.setPositiveButton(getString(R.string.exit), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		exitAlertDialog.show();
	}

	private void checkForCrashes() {
		CrashManager.register(this, "372550dc8bd683a8457c9f793430ed99");
	}

	private void checkForUpdates() {
		// Remove this for store builds!
		UpdateManager.register(this, "372550dc8bd683a8457c9f793430ed99");
	}

}
