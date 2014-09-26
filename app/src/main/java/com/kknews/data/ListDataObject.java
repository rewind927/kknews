package com.kknews.data;

/**
 * Created by ryanwang on 2014/9/22.
 */
public class ListDataObject {
	String url;
	String title;

	public ListDataObject(String url, String title) {
		this.url = url;
		this.title = title;
	}

	public String getUrl() {
		return url;
	}

	public String getTitle() {
		return title;
	}
}
