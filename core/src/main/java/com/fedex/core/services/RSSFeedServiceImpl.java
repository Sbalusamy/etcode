package com.fedex.core.services;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.fedex.core.models.FedexConstants;
import com.fedex.core.models.News;
import com.fedex.core.models.NewsFeedVO;
import com.fedex.core.models.NewsItems;

@Component(service = RSSFeedService.class, immediate = true)
@Designate(ocd = RSSFeedConfiguration.class)
public class RSSFeedServiceImpl implements RSSFeedService {

	private static final String CONTENT_NODE_TYPE = "nt:unstructured";
	@Reference
	private ConfigurationAdmin configAdmin;

	@Reference
	ResourceResolverFactory resourceResolverFactory;

	@Reference
	QueryBuilder queryBuilder;

	private String rssNodeLocation;
	private String rssNodeName;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Activate
	@Modified
	protected void activate(final RSSFeedConfiguration config) {
		rssNodeLocation = config.rssNodeLocation();
		rssNodeName = config.rssNodeName();
	}

	public List<NewsFeedVO> getNewsFeed(boolean mostRecentFeed, String noOfFeeds) {
		final Map<String, Object> paramMap = new HashMap<String, Object>();
		List<NewsFeedVO> feedItem = new ArrayList<>();
		paramMap.put(ResourceResolverFactory.SUBSERVICE, "contentWriter");
		try (ResourceResolver resolver = resourceResolverFactory.getServiceResourceResolver(paramMap)) {
			Resource newsNode = resolver.getResource(rssNodeLocation + "/" + rssNodeName);
			if (newsNode != null && newsNode.hasChildren()) {

				/*
				 * final Iterator<Resource> list = newsNode.listChildren(); while
				 * (list.hasNext()) { final Resource childResource = list.next();
				 * logger.info("entered dynamic {}", childResource.getName()); final NewsFeedVO
				 * newsFeedItemList = new NewsFeedVO();
				 * newsFeedItemList.setTitle(childResource.getValueMap().get(FedexConstants.
				 * TITLE, StringUtils.EMPTY));
				 * newsFeedItemList.setDesc(childResource.getValueMap().get(FedexConstants.DESC,
				 * StringUtils.EMPTY)); newsFeedItemList.setPublishedDate(
				 * childResource.getValueMap().get(FedexConstants.PUBLISHED_DATE,
				 * StringUtils.EMPTY)); feedItem.add(newsFeedItemList); }
				 */

				String noOfResults = "";
				if (mostRecentFeed) {
					noOfResults = "1";
				} else if (StringUtils.isNotBlank(noOfFeeds)) {
					noOfResults = noOfFeeds;
				}
				feedItem = getNewsFeed(feedItem, resolver, noOfResults);
			}
		} catch (RepositoryException e) {
			logger.error("LoginException {}", e.getMessage());
		} catch (LoginException e) {

		} catch (Exception e) {
			logger.error("Exception {}", e.getMessage());
		}
		return feedItem;

	}

	private List<NewsFeedVO> getNewsFeed(List<NewsFeedVO> feedItem, ResourceResolver resolver, String noOfResults)
			throws RepositoryException {
		Map<String, String> map = new HashMap<>();
		map.put("orderby", "@" + FedexConstants.PUBLISHED_DATE);
		map.put("orderby.sort", "desc");
		map.put("path", rssNodeLocation + "/" + rssNodeName);
		map.put("type", CONTENT_NODE_TYPE);
		if (StringUtils.isNotBlank(noOfResults)) {
			map.put("p.limit", noOfResults);
		}

		PredicateGroup predicateGroup = PredicateGroup.create(map);
		Query query = queryBuilder.createQuery(predicateGroup, resolver.adaptTo(Session.class));

		SearchResult result = query.getResult();
		List<Hit> hits = result.getHits();

		for (Hit hit : hits) {
			final NewsFeedVO newsFeedItemList = new NewsFeedVO();
			ValueMap properties = hit.getProperties();
			newsFeedItemList.setTitle(properties.get(FedexConstants.TITLE, StringUtils.EMPTY));
			newsFeedItemList.setDesc(properties.get(FedexConstants.DESC, StringUtils.EMPTY));
			newsFeedItemList.setPublishedDate(properties.get(FedexConstants.PUBLISHED_DATE, StringUtils.EMPTY));
			feedItem.add(newsFeedItemList);
		}
		return feedItem;
	}

	public void addRssDetailsNode(News news) {

		final Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put(ResourceResolverFactory.SUBSERVICE, "contentWriter");
		try (ResourceResolver resolver = resourceResolverFactory.getServiceResourceResolver(paramMap)) {
			Session session = resolver.adaptTo(Session.class);

			logger.info("News Item Size {}", news.getChannel().getNewsItems().size());
			for (NewsItems newsItem : news.getChannel().getNewsItems()) {
				if (session.nodeExists(rssNodeLocation + rssNodeName)) {
					addNewsNode(session, rssNodeLocation + "/" + rssNodeName, newsItem);
				} else {
					addRSSRootNode(session, rssNodeLocation, rssNodeName);
					addNewsNode(session, rssNodeLocation + "/" + rssNodeName, newsItem);
				}
			}
		} catch (RepositoryException e) {
			logger.error("RepositoryException {}", e.getMessage());
		} catch (org.apache.sling.api.resource.LoginException e) {
			logger.error("LoginException {}", e.getMessage());
		}
	}

	private String addNewsNode(Session session, String rssContentPath, NewsItems newsItem) {
		try {
			if (session.nodeExists(rssContentPath)) {
				Node rssRootNode = session.getNode(rssContentPath);
				if (!rssRootNode.hasNode(newsItem.getGuid())) {
					Node parentNode = rssRootNode.addNode(newsItem.getGuid(), CONTENT_NODE_TYPE);
					parentNode.setProperty(FedexConstants.TITLE, newsItem.getTitle());
					parentNode.setProperty(FedexConstants.DESC, newsItem.getDescription());
					final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
					parentNode.setProperty(FedexConstants.PUBLISHED_DATE,
							dateFormat.format(newsItem.getPublishedDate()));
					logger.info("published date {}", newsItem.getPublishedDate());
					logger.info("published date {}", dateFormat.format(newsItem.getPublishedDate()));
					parentNode.setProperty(FedexConstants.LINK, newsItem.getLink());
					session.save();
					return parentNode.getName();
				}
			}
		} catch (Exception e) {
		}
		return null;
	}

	private String addRSSRootNode(Session session, String rssNodeLocation, String rssNodeName) {
		try {
			if (session.nodeExists(rssNodeLocation)) {
				Node rssParentNode = session.getNode(rssNodeLocation);
				if (!rssParentNode.hasNode(rssNodeName)) {
					Node parentNode = rssParentNode.addNode(rssNodeName, JcrResourceConstants.NT_SLING_FOLDER);
					session.save();
					return parentNode.getName();
				}
			}
		} catch (Exception e) {
			logger.error("erro msg {}", e.toString());
		}
		return null;
	}

}
