package com.kknews.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.ryanwang.helloworld.R;
import com.kknews.fragment.HotFragment;
import com.kknews.fragment.PersonalFragment;
import com.kknews.fragment.SettingFragment;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

public class MyActivity extends FragmentActivity {

	private static final int HOT_FRAGMENT_TAG = 0;
	private static final int PERSONAL_FRAGMENT_TAG = 1;
	private static final int SETTING_FRAGMENT_TAG = 2;

	private int currentFragment = HOT_FRAGMENT_TAG;

	private String[] mPlanetTitles = {"嚴選專欄", "個人精選", "設定"};

	//drawer
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my);

		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		mDrawerToggle = new ActionBarDrawerToggle(
				this,                  /* host Activity */
				mDrawerLayout,         /* DrawerLayout object */
				R.drawable.ic_navigation_drawer,  /* nav drawer icon to replace 'Up' caret */
				R.string.hot,  /* "open drawer" description */
				R.string.setting  /* "close drawer" description */
		) {
			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
			}

			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		mDrawerList.setAdapter(new ArrayAdapter<String>(this,
				R.layout.drawer_list_item, mPlanetTitles));
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		initHotFragment();
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

				FragmentManager fm = getSupportFragmentManager();
				if (fm.getBackStackEntryCount() > 0) {
					fm.popBackStack();
					setDrawerIndicatorEnable(true);
				} else {
					if (mDrawerToggle.onOptionsItemSelected(item)) {
						return true;
					}
				}
				break;
		}
		return false;
	}

	@Override
	public MenuInflater getMenuInflater() {
		return super.getMenuInflater();
	}

	@Override
	public void onBackPressed() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
		FragmentManager fm = getSupportFragmentManager();
		if (fm.getBackStackEntryCount() > 0) {
			super.onBackPressed();
			setDrawerIndicatorEnable(true);
		} else {
			showExitDialog();
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	private void showExitDialog() {
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

	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView parent, View view, int position, long id) {
			selectItem(position);
		}
	}

	private void selectItem(int position) {
		if (currentFragment == position) {
			mDrawerLayout.closeDrawer(mDrawerList);
			return;
		}

		Fragment fragment = null;

		currentFragment = position;

		switch (position) {
			case HOT_FRAGMENT_TAG:
				fragment = new HotFragment();
				break;
			case PERSONAL_FRAGMENT_TAG:
				fragment = new PersonalFragment();
				break;
			case SETTING_FRAGMENT_TAG:
				fragment = new SettingFragment();
				break;
			default:
				fragment = new HotFragment();
				break;
		}
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction()
				.replace(R.id.content_frame, fragment)
				.commit();

		mDrawerList.setItemChecked(position, true);
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	private void initHotFragment() {
		Fragment fragment = new HotFragment();
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction()
				.replace(R.id.content_frame, fragment)
				.commit();
	}

	public void setDrawerIndicatorEnable(boolean enable){
		mDrawerToggle.setDrawerIndicatorEnabled(enable);
	}

}
