package com.kknews.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.ryanwang.helloworld.R;
import com.kknews.activity.MyActivity;
import com.kknews.data.ListDataObject;
import com.kknews.service.GetMetaDataService;
import com.kknews.util.Def;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by ryanwang on 2014/9/25.
 */
public class HotFragment extends Fragment {
	private static final String TAG = "HotFragment";
	private ListView listViewHotEntry;
	private HotEntryAdapter adapterHotEntry;

	private ArrayList<ListDataObject> dataList;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.layout_hot, container, false);

		((MyActivity)getActivity()).setDrawerIndicatorEnable(true);

		listViewHotEntry = (ListView) view.findViewById(R.id.listview_hot_entry);
		listViewHotEntry.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				FragmentManager fragmentManager = getFragmentManager();
				FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
				fragmentTransaction.addToBackStack(null);
				HotContentFragment fragment = new HotContentFragment();
				Bundle bundle = new Bundle();
				bundle.putString(Def.PASS_URL_KEY, dataList.get(i).getUrl());
				bundle.putString(Def.PASS_TITLE_KEY, dataList.get(i).getTitle());
				fragment.setArguments(bundle);
				fragmentTransaction.add(R.id.content_frame, fragment);
				fragmentTransaction.commit();

			}
		});
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		new Thread() {
			@Override
			public void run() {
				getExtractList(Def.RSS_SUB_URL, new ParseHotEntryCallback() {

					@Override
					public void onFinish(ArrayList<ListDataObject> dataList) {
						if (listViewHotEntry != null) {
							mHandler.sendEmptyMessage(3);
						}
						Log.d(TAG, "dtatList.size:" + dataList.size());
					}
				});
			}
		}.start();
		super.onActivityCreated(savedInstanceState);

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}


	interface ParseHotEntryCallback {
		void onFinish(ArrayList<ListDataObject> dataList);
	}

	private void getExtractList(String url, ParseHotEntryCallback callback) {
		try {
			Document doc = Jsoup.connect(url).get();

			Elements metaElems = doc.select("div.span2");
			dataList = parseHotElements(metaElems);
			Log.d(TAG, "metaElems:" + metaElems.size());
			callback.onFinish(dataList);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private ArrayList<ListDataObject> parseHotElements(Elements elements) {
		ArrayList<ListDataObject> dataList = new ArrayList<ListDataObject>();
		for (org.jsoup.nodes.Element element : elements) {
			if (element.text().startsWith("嚴選專欄")) {
				Elements subElements = element.select("a[href]");
				for (Element e : subElements) {
					ListDataObject data = new ListDataObject(e.attr("href"), e.text());
					dataList.add(data);
					Log.d(TAG, "e:" + e.text());
					Log.d(TAG, "url:" + e.attr("href"));
				}
			}
		}
		return dataList;
	}

	class HotEntryAdapter extends BaseAdapter {

		private LayoutInflater inflater;

		HotEntryAdapter(Context ctx) {
			inflater = LayoutInflater.from(ctx);
		}

		@Override
		public int getCount() {
			if (dataList == null) {
				return 0;
			}
			return dataList.size();
		}

		@Override
		public Object getItem(int i) {
			return dataList.get(i);
		}

		@Override
		public long getItemId(int i) {
			return 0;
		}

		@Override
		public View getView(int i, View convertView, ViewGroup viewGroup) {
			ViewHolder holder = null;

			if (convertView == null){
				holder = new ViewHolder();
				convertView = inflater.inflate(R.layout.item, null);
				holder.textTitle = (TextView) convertView.findViewById(R.id.text_title);
				convertView.setTag(holder);
			}else {
				holder = (ViewHolder) convertView.getTag();
			}


			holder.textTitle.setText(dataList.get(i).getTitle());

			return convertView;
		}
	}

	class ViewHolder {
		TextView textTitle;
	}

	Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if (getActivity() == null) {
				return;
			}
			if (adapterHotEntry == null) {
				adapterHotEntry = new HotEntryAdapter(getActivity());
			}
			listViewHotEntry.setAdapter(adapterHotEntry);

			Intent startIntent = new Intent(getActivity(), GetMetaDataService.class);
			Bundle bundle = new Bundle();

			ArrayList<String> urls = new ArrayList<String>();
			ArrayList<String> titles = new ArrayList<String>();

			for (int i=0;i< dataList.size();i++){
				urls.add(dataList.get(i).getUrl());
				titles.add(dataList.get(i).getTitle());
			}
			bundle.putStringArrayList("url",urls);
			bundle.putStringArrayList("titles",titles);
			startIntent.putExtras(bundle);

			getActivity().startService(startIntent);
		}
	};
}
