package com.fedex.core.models;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fedex.core.services.RSSFeedService;

@Model(adaptables = { Resource.class, SlingHttpServletRequest.class }, adapters = { NewsFeed.class })
public class NewsFeedImpl implements NewsFeed {

	private static final Logger LOG = LoggerFactory.getLogger(NewsFeedImpl.class);
	public static final String NEWS_FEED_ITEMS = "feedDetails";

	@ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
	private String rssFeedType;

	@ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
	private boolean mostRecent;

	@ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
	private String feedNo;

	@SlingObject
	@Optional
	private Resource res;

	@SlingObject
	ResourceResolver resourceResolver;

	@OSGiService
	RSSFeedService rssFeedService;

	@Override
	public List<NewsFeedVO> getNewsFeedItems() {		
		List<NewsFeedVO> feedItem = new ArrayList<>();
		if (rssFeedType.equals("dynamic")) {			
			feedItem = rssFeedService.getNewsFeed(mostRecent, feedNo);
		} else if (rssFeedType.equals("static")) {
			feedItem = getStaticFeed();
		}
		return Collections.unmodifiableList(feedItem);
	}

	private List<NewsFeedVO> getStaticFeed() {
		List<NewsFeedVO> feedItem = new ArrayList<>();
		final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		final Resource feedList = res.getChild(NEWS_FEED_ITEMS);
		if (feedList != null && feedList.hasChildren()) {
			final Iterator<Resource> list = feedList.listChildren();
			while (list.hasNext()) {
				final Resource childResource = list.next();
				final NewsFeedVO newsFeedItemList = new NewsFeedVO();
				newsFeedItemList.setTitle(childResource.getValueMap().get(FedexConstants.TITLE, StringUtils.EMPTY));
				newsFeedItemList.setDesc(childResource.getValueMap().get(FedexConstants.DESC, StringUtils.EMPTY));
				GregorianCalendar publishedDate = (GregorianCalendar) childResource.getValueMap()
						.get(FedexConstants.PUBLISHED_DATE);
				newsFeedItemList.setPublishedDate(dateFormat.format(publishedDate.getTime()));
				feedItem.add(newsFeedItemList);
			}
		} else {
			LOG.error("Feed list is empty {}", res.getPath());
		}
		return feedItem;
	}

	@Override
	public int getNewsFeedItemsSize() {
		final int defaultSize = 0;
		if (getNewsFeedItems() != null) {
			return getNewsFeedItems().size();
		}
		return defaultSize;
	}

}
