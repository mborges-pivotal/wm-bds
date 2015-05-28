/*=========================================================================
 * Copyright (c) 2010-2014 Pivotal Software, Inc. All rights reserved.
 * This product is protected by U.S. and international copyright
 * and intellectual property laws. Pivotal products are covered by
 * one or more patents listed at http://www.gopivotal.com/patents.
 *=========================================================================
 */
package com.gopivotal.tola.wm.util.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Arrays;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


/**
 * Util - utilities to check input files
 * 
 * @author mborges
 *
 */
@Component
public class UtilService {
	
	private final static Logger logger = LoggerFactory.getLogger(UtilService.class); 	
	
	/* Used for troubleshooting input files
	 * try { checkFile(new
	 * File("/Users/mborges/Work/POCs/wm-bds/wm/data/opt",
	 * "MMB_ar_slxtr.csv")); } catch (IOException e1) { // TODO
	 * Auto-generated catch block e1.printStackTrace(); } System.exit(0);
	 */	
	
	/**
	 * Load properties files in classpath into Map
	 * 
	 * @param name
	 * @param map
	 * @throws IOException
	 */
	public void loadProps(String name, Map<String, String> map)
			throws IOException {
		
		logger.info("Loading props file {}", name); 
	
		Properties prop = new Properties();
		InputStream in = this.getClass().getResourceAsStream(name);
		prop.load(in);
		in.close();

		for (final String key : prop.stringPropertyNames()) {
			map.put(key, prop.getProperty(key));
		}
	}
	

	
	/**
	 * Check input file
	 */
	public void checkFile(File fin) throws IOException {
		// Construct BufferedReader from FileReader
		BufferedReader br = new BufferedReader(new FileReader(fin));

		List<String> cols = new ArrayList<String>();

		String line = null;
		int count = 0;
		while ((line = br.readLine()) != null) {
			System.out.println(line);

			// ParseHeader
			if (count == 0) {
				cols.addAll(Arrays.asList(line.split("\\|")));
				System.out.printf("%d header columns\n", cols.size());
			} else {
				String[] values = line.split("\\|");
				System.out.printf("%d columns\n", values.length);
				for (int i = 0; i < cols.size(); i++) {
					System.out.printf("%s -> '%s'\n", cols.get(i), values[i]);
				}
			}

			count++;
		}

		br.close();
	}
	

}
