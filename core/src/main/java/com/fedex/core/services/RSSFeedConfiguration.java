package com.fedex.core.services;


import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "Content Node to store RSS News Feed", description = "Path to store RSS News")
public @interface RSSFeedConfiguration {

  @AttributeDefinition(name = "Node Location", description = "Node Location")
  String rssNodeLocation() default "/content";

  @AttributeDefinition(name = "Node Name", description = "Node Name")
  String rssNodeName() default "news_details";
}
