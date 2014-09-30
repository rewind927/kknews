package com.kknews.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;

import com.example.ryanwang.helloworld.R;
import com.kknews.adapter.ViewPagerAdapter;
import com.kknews.fragment.HotFragment;
import com.kknews.fragment.PersonalFragment;
import com.kknews.fragment.SettingFragment;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class MyActivity extends FragmentActivity implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {

	private TabHost mTabHost;
	private ViewPager mViewPager;
	private HashMap<String, TabInfo> mapTabInfo = new HashMap<String, TabInfo>();
	private ViewPagerAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my);

		initialiseTabHost(savedInstanceState);
		if (savedInstanceState != null) {
			mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab")); //set the tab as per the saved state
		}
		intialiseViewPager();

	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.action_menu, menu);
//		return false;
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// Handle action bar item clicks here. The action bar will
//		// automatically handle clicks on the Home/Up button, so long
//		// as you specify a parent activity in AndroidManifest.xml.
//		int id = item.getItemId();
//		if (id == R.id.action_add_my_favorite) {
//			return true;
//		}
//		return super.onOptionsItemSelected(item);
//	}

	@Override
	public MenuInflater getMenuInflater() {
		return super.getMenuInflater();
	}

	private void getXml(String url) {
		try {
			Document doc = Jsoup.connect(url).get();

			Elements metaElements = doc.select("item");
			Log.d("123", "metaElements:" + metaElements.size());

			for (Element el : metaElements) {
				for (Element subEl : el.children()) {
					Log.d("123", subEl.tag() + ":" + subEl.text());

				}
				Log.d("123", "------------------------");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
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
	}

}
