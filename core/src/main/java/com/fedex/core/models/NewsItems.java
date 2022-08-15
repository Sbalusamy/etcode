package com.fedex.core.models;

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;

public class NewsItems {

	private String description;

	private String title;

	private String link;

	private Date publishedDate;

	private String guid;

	@XmlElement(name = "guid")
	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	@XmlElement(name = "link")
	public String getLink() {
		return this.link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	@XmlElement(name = "updated", namespace = "http://www.w3.org/2005/Atom")
	public Date getPublishedDate() {
		return this.publishedDate;
	}

	public void setPublishedDate(Date publishedDate) {
		this.publishedDate = publishedDate;
	}

	@XmlElement(name = "description")
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@XmlElement(name = "title")
	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public NewsItems(String description, String title) {
		this.description = description;
		this.title = title;
	}

	public NewsItems() {
	}

	@Override
	public String toString() {
		return "NewsItems [description=" + description + ", title=" + title + ", link=" + link + ", publishedDate="
				+ publishedDate + ", guid=" + guid + "]";
	}

}
