/*
 *  Copyright 2015 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.fedex.core.servlets;

import com.day.cq.commons.jcr.JcrConstants;
import com.fedex.core.models.NewsItems;
import com.google.common.collect.ImmutableMap;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Servlet that writes some sample content into the response. It is mounted for
 * all resources of a specific Sling resource type. The
 * {@link SlingSafeMethodsServlet} shall be used for HTTP methods that are
 * idempotent. For write operations use the {@link SlingAllMethodsServlet}.
 */
@Component(service = { Servlet.class })
@SlingServletResourceTypes(
        resourceTypes="fedex/components/page",
        methods=HttpConstants.METHOD_GET,
        selectors = "writenode", extensions = "html")
@ServiceDescription("Simple Demo Servlet")
public class SimpleServlet extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = 1L;
    private final Logger logger = LoggerFactory.getLogger(getClass());

	@Reference
	ResourceResolverFactory resourceResolverFactory;
	
    @Override
    protected void doGet(final SlingHttpServletRequest req,
            final SlingHttpServletResponse resp) throws ServletException, IOException {
//final Map<String, Object> authInfo = ImmutableMap.of(ResourceResolverFactory.SUBSERVICE, "contentWriter");
final Map<String, Object> paramMap = new HashMap<String, Object>();
paramMap.put( ResourceResolverFactory.SUBSERVICE, "contentWriter" );
		try (ResourceResolver resolver = resourceResolverFactory.getServiceResourceResolver(paramMap)) {
			Session session = resolver.adaptTo(Session.class);
			String nodeCreated;
			Node rssParentNode = session.getNode("/content");
			logger.info("god help me 123{}",rssParentNode.getPath());
			Node rssNodeName =rssParentNode.addNode("sindhu");
			session.save();
			logger.info("god",rssNodeName);
			session.save();
			
		} catch (RepositoryException e) {
			logger.error("RepositoryException {}",e.toString());
		} catch (org.apache.sling.api.resource.LoginException e) {
			logger.error("LoginException {}", e.getMessage());
		}
		catch (Exception e) {
			logger.error("Exception {}", e.getMessage());
		}
    }
}
