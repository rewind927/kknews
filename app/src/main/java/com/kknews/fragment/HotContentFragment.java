package com.kknews.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ryanwang.helloworld.R;
import com.kknews.data.ContentDataObject;
import com.kknews.util.CustomExpandCard;
import com.kknews.util.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.view.CardListView;

/**
 * Created by ryanwang on 2014/9/25.
 */
public class HotContentFragment extends Fragment {

	public final static String TAG = "HotContentFragment";

	private ListView mListViewHotContent;
	private HotEntryAdapter mAdapterHotEntry;

	private ArrayList<ContentDataObject> mDataList;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.layout_hot_content, container, false);
		TextView t = (TextView) view.findViewById(R.id.text_hot_title);
		Bundle bundle = this.getArguments();
		t.setText(bundle.getString(Utils.PASS_TITLE_KEY,null));
		mListViewHotContent = (ListView) view.findViewById(R.id.listview_hot_content);
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		Bundle bundle = this.getArguments();
		final String dataUrl = bundle.getString(Utils.PASS_URL_KEY, null);

		new Thread() {
			@Override
			public void run() {
				getData(dataUrl);
			}
		}.start();

		super.onCreate(savedInstanceState);
	}

	private void getData(String url) {
		mDataList = getXml(url);
		mHandler.sendEmptyMessage(3);
	}

	private ArrayList<ContentDataObject> getXml(String url) {
		ArrayList<ContentDataObject> dataList = null;

		try {
			Document doc = Jsoup.connect(url).get();

			Elements metaElements = doc.select("item");
			Log.d(TAG, "metaElements:" + metaElements.size());

			dataList = new ArrayList<ContentDataObject>();

			for (Element el : metaElements) {
				ContentDataObject data = new ContentDataObject();

				for (Element subEl : el.children()) {
					if (subEl.tag().toString().equals(Utils.HOT_CONTENT_TITLE)) {
						data.setTitle(subEl.text());
					} else if (subEl.tag().toString().equals(Utils.HOT_CONTENT_CATEGORY)) {
						data.setCategory(subEl.text());
					} else if (subEl.tag().toString().equals(Utils.HOT_CONTENT_GUID)) {
						data.setLink(subEl.text());
					} else if (subEl.tag().toString().equals(Utils.HOT_CONTENT_DATE)) {
						data.setDate(subEl.text());
					} else if (subEl.tag().toString().equals(Utils.HOT_CONTENT_DESCRIPTION)) {
						String html = subEl.text();
						Document docDescription = Jsoup.parse(html);
						Elements elements = docDescription.select("img");
						data.setImgUrl(elements.get(0).attr("src"));
						elements = docDescription.select(" 文章內容 ");
						Log.d(TAG,"elements.get(0).text():"+elements.size()+",");
						data.setDescription(subEl.text());
					}
					Log.d(TAG, subEl.tag() + ":" + subEl.text());

				}
				dataList.add(data);
				Log.d(TAG, "------------------------");
			}

		} catch (IOException e) {
			e.printStackTrace();
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
			if (mDataList == null) {
				return 0;
			}
			return mDataList.size();
		}

		@Override
		public Object getItem(int i) {
			return mDataList.get(i);
		}

		@Override
		public long getItemId(int i) {
			return 0;
		}

		@Override
		public View getView(int i, View convertView, ViewGroup viewGroup) {
			ViewHolder holder = null;

			if (convertView == null) {
				holder = new ViewHolder();
				convertView = inflater.inflate(R.layout.layout_content_item, null);
				holder.textTitle = (TextView) convertView.findViewById(R.id.text_title);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.textTitle.setText(mDataList.get(i).getTitle());
			Log.w("123", "mDataList.get(i).getTitle():" + mDataList.get(i).getTitle());

			return convertView;
		}
	}

	class ViewHolder {
		TextView textTitle;
	}

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
//			mAdapterHotEntry = new HotEntryAdapter(getActivity());
//			mListViewHotContent.setAdapter(mAdapterHotEntry);
			initCards();
		}
	};

	private void initCards() {

		//Init an array of Cards
		ArrayList<Card> cards = new ArrayList<Card>();
		for (int i=0;i<mDataList.size();i++){
			Card card = init_standard_header_with_expandcollapse_button_custom_area(mDataList.get(i).getTitle(),i);
			cards.add(card);
		}

		CardArrayAdapter mCardArrayAdapter = new CardArrayAdapter(getActivity(),cards);

		CardListView listView = (CardListView) getActivity().findViewById(R.id.carddemo_list_expand);
		if (listView!=null){
			listView.setAdapter(mCardArrayAdapter);
		}
	}


	/**
	 * This method builds a standard header with a custom expand/collpase
	 */
	private Card init_standard_header_with_expandcollapse_button_custom_area(String titleHeader,int i) {

		//Create a Card
		Card card = new Card(getActivity());

		//Create a CardHeader
		CardHeader header = new CardHeader(getActivity());

		//Set the header title
		header.setTitle(titleHeader);

		//Set visible the expand/collapse button
		header.setButtonExpandVisible(true);

		//Add Header to card
		card.addCardHeader(header);

		//This provides a simple (and useless) expand area
		CustomExpandCard expand = new CustomExpandCard(getActivity(),i);
		//Add Expand Area to Card
		card.addCardExpand(expand);

		//Just an example to expand a card
		if (i==2 || i==7 || i==9)
			card.setExpanded(true);

		//Swipe
		card.setSwipeable(true);

		//Animator listener
		card.setOnExpandAnimatorEndListener(new Card.OnExpandAnimatorEndListener() {
			@Override
			public void onExpandEnd(Card card) {
				Toast.makeText(getActivity(), "Expand " + card.getCardHeader().getTitle(), Toast.LENGTH_SHORT).show();
			}
		});

		card.setOnCollapseAnimatorEndListener(new Card.OnCollapseAnimatorEndListener() {
			@Override
			public void onCollapseEnd(Card card) {
				Toast.makeText(getActivity(),"Collpase " +card.getCardHeader().getTitle(),Toast.LENGTH_SHORT).show();
			}
		});

		return card;
	}
}
