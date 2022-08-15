package com.fedex.core.models;

import java.util.List;

public interface NewsFeed {

	/**
	 * @return a collection of objects representing the table items that compose the
	 *         list.
	 */
	List<NewsFeedVO> getNewsFeedItems();

	int getNewsFeedItemsSize();

}
