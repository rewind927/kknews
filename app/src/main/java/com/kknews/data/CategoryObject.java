package com.kknews.data;

/**
 * Created by ryanwang on 2014/9/22.
 */
public class CategoryObject {
	private String category;
	private String imgUrl;

	public CategoryObject() {
	}

	public CategoryObject(String link, String guid, String date, String description, String title, String category, String imgUrl) {
		this.category = category;
		this.imgUrl = imgUrl;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getCategory() {
		return category;
	}

	public String getImgUrl() {
		return imgUrl;
	}

	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}
}
