package com.fedex.core.services;

import java.util.List;

import com.fedex.core.models.News;
import com.fedex.core.models.NewsFeedVO;

public interface RSSFeedService {

 public void addRssDetailsNode(News news);
 public List<NewsFeedVO> getNewsFeed(boolean mostRecentFeed, String noOfFeeds);
	
}
