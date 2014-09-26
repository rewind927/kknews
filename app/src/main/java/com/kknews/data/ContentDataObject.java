package com.kknews.data;

/**
 * Created by ryanwang on 2014/9/22.
 */
public class ContentDataObject {
	private String link;
	private String guid;
	private String date;
	private String description;
	private String title;
	private String category;
	private String imgUrl;

	public ContentDataObject() {
	}

	public ContentDataObject(String link, String guid, String date, String description, String title, String category, String imgUrl) {
		this.link = link;
		this.guid = guid;
		this.date = date;
		this.description = description;
		this.title = title;
		this.category = category;
		this.imgUrl = imgUrl;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getLink() {
		return link;
	}

	public String getDate() {
		return date;
	}

	public String getDescription() {
		return description;
	}

	public String getTitle() {
		return title;
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
