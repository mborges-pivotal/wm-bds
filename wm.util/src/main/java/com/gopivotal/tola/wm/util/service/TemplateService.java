/*=========================================================================
 * Copyright (c) 2010-2014 Pivotal Software, Inc. All rights reserved.
 * This product is protected by U.S. and international copyright
 * and intellectual property laws. Pivotal products are covered by
 * one or more patents listed at http://www.gopivotal.com/patents.
 *=========================================================================
 */
package com.gopivotal.tola.wm.util.service;

import java.io.Writer;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.springframework.stereotype.Component;

/**
 * TemplateService - run templates for delta process
 * 
 * @author mborges
 *
 */
@Component
public class TemplateService {

	private VelocityEngine ve = new VelocityEngine();
	private VelocityContext context = new VelocityContext();

	/**
	 * constructor
	 */
	public TemplateService() {
		ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		ve.setProperty("classpath.resource.loader.class",
				ClasspathResourceLoader.class.getName());
		ve.init();		
	}
	
	public void addContext(String symbol, Object o) {
		context.put(symbol, o);
	}
	
	/**
	 * Run template
	 */
	public void runTemplate(Writer writer, String template) {
		Template t = ve.getTemplate(template);		
		t.merge(context, writer);
	}


}
