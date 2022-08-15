package com.fedex.core.schedulers;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fedex.core.models.News;
import com.fedex.core.services.RSSFeedService;

/**
 * A simple demo for cron-job like tasks that get executed regularly. It also
 * demonstrates how property values can be set. Users can set the property
 * values in /system/console/configMgr
 */
@Designate(ocd = RSSFeedScheduler.Config.class)
@Component(service = Runnable.class)
public class RSSFeedScheduler implements Runnable {

	@ObjectClassDefinition(name = "RSS Feed Scheduler Configuration", description = "Simple demo for rss feed")
	public static @interface Config {

		@AttributeDefinition(name = "Cron-job expression")
		String scheduler_expression() default "* * 23 * * ?";

		@AttributeDefinition(name = "Concurrent task", description = "Whether or not to schedule this task concurrently")
		boolean scheduler_concurrent() default false;

		@AttributeDefinition(name = "RSS FEED API", description = "")
		String rssFeedAPI() default "https://sports.ndtv.com/rss/cricket";

		@AttributeDefinition(name = "Environment", description = "")
		String environment() default "author";
	}

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private String rssFeedAPI;
	private String environment;

	@Reference
	RSSFeedService rssFeedService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		logger.info("RSSScheduledTask is now running, myParameter='{}'", rssFeedAPI);
		if (environment.equals("author")) {
			JAXBContext jaxbContext;
			try {
				logger.info("URL {}", rssFeedAPI);
				URL url = new URL(rssFeedAPI);
				logger.info(url.toString());
				HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
				InputStream is = con.getInputStream();
				jaxbContext = JAXBContext.newInstance(News.class);
				Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
				News news = (News) jaxbUnmarshaller.unmarshal(is);
				rssFeedService.addRssDetailsNode(news);
				logger.info("News Items {}", news.getChannel().getNewsItems().toString());
			} catch (JAXBException e) {
				logger.info("JAXBException {}", e.getMessage());
			} catch (MalformedURLException e) {
				logger.info("MalformedURLException {}", e.getMessage());
			} catch (IOException e) {
				logger.info("IOException {}", e.getMessage());
			}
		}
	}

	@Activate
	@Modified
	protected void activate(final Config config) {
		rssFeedAPI = config.rssFeedAPI();
		environment = config.environment();
	}

}
