package com.fedex.core.models;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class Channel {

	protected List<NewsItems> newsItems = new ArrayList<NewsItems>();

	@XmlElement(name = "item")
	public List<NewsItems> getNewsItems() {
		return this.newsItems;
	}

	public void setNewsItems(List<NewsItems> newsItems) {
		this.newsItems = newsItems;
	}

	public Channel() {
	}

}
