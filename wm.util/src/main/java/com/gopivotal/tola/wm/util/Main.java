/*=========================================================================
 * Copyright (c) 2010-2014 Pivotal Software, Inc. All rights reserved.
 * This product is protected by U.S. and international copyright
 * and intellectual property laws. Pivotal products are covered by
 * one or more patents listed at http://www.gopivotal.com/patents.
 *=========================================================================
 */
package com.gopivotal.tola.wm.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

/**
 * Generates ELT artifacts
 * 
 * @see http
 *      ://www.javaworld.com/article/2075966/core-java/start-up-the-velocity-
 *      template-engine.html
 * 
 * @author mborges
 *
 */
public class Main {

	private static Map<String, String> typeMap = null;
	private static final long serialVersionUID = 1L;
	private static Map<String, String> tablePk = null;
	private static Map<String, String> appProps = null;

	/**
	 * Main - generate delta scripts
	 * 
	 * @param args - T=tables, L=load, C=clear, A=all (default)
	 */
	public static void main(String[] args) {

		/* Used for troubleshooting input files
		 * try { checkFile(new
		 * File("/Users/mborges/Work/POCs/wm-bds/wm/data/opt",
		 * "MMB_ar_slxtr.csv")); } catch (IOException e1) { // TODO
		 * Auto-generated catch block e1.printStackTrace(); } System.exit(0);
		 */
		
		String argTablename = null;
		char arg = 'B';
		if (args.length > 0) {
			arg = args[0].toUpperCase().charAt(0);
			if (args.length > 1) {
				argTablename = args[1];
			}
		}

		/* first, get and initialize an engine */
		VelocityEngine ve = new VelocityEngine();
		ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		ve.setProperty("classpath.resource.loader.class",
				ClasspathResourceLoader.class.getName());

		ve.init();

		// Loop to all the tables
		for (String tableName : tablePk.keySet()) {
			
			if (argTablename != null && !argTablename.equalsIgnoreCase(tableName)) {
				continue;
			}

			Map<String, String> colsMap = null;
			Map<String, Boolean> colsIsKey = null;

			/* Catalog */
			try {
				colsMap = getTable(tableName);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			colsIsKey = mapKeys(colsMap,tablePk.get(tableName));

			VelocityContext context = new VelocityContext();
			context.put("tablename", tableName);
			context.put("cols_isKey", colsIsKey);
			context.put("cols_types", colsMap);

			switch(arg) {
			case 'C':
				runTemplate("clear.vm", ve, context);
				break;
			case 'T':
				runTemplate("tables.vm", ve, context);
				break;
			case 'L':
				runTemplate("load.vm", ve, context);
				break;
			default:
				runTemplate("clear.vm", ve, context);
				runTemplate("tables.vm", ve, context);
				runTemplate("load.vm", ve, context);
			}

		} // for each table
		
	} // main

	/**
	 * Check input file
	 */
	private static void checkFile(File fin) throws IOException {
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

	/**
	 * get Table information
	 * 
	 * TODO: Use pg_catalog to get distribution and other information
	 * 
	 * @throws SQLException
	 */
	private static Map<String, String> getTable(String tableName)
			throws SQLException {

		String fixSizeType = "BIGINT,TIMESTAMP,DATE";

		Connection conn = DriverManager.getConnection(
				appProps.get("jdbc_url"), appProps.get("username"), appProps.get("password"));
		DatabaseMetaData metadata = conn.getMetaData();
		// ResultSet rs = metadata.getTables(null, null, tableName, null); //
		// catalog, schemaPattern, tableNamePattern, types)
		ResultSet rs = metadata.getColumns(null, null, tableName, null); // catalog,
																			// schemaPattern,
																			// tableNamePattern,
																			// types)
		Map<String, String> colsMap = new LinkedHashMap<String, String>();
		while (rs.next()) {
			String decimalDigits = rs.getString("DECIMAL_DIGITS");
			String colType = typeMap(rs.getString("TYPE_NAME").toUpperCase());
			StringBuffer colTypeSb = new StringBuffer(colType);

			if (fixSizeType.indexOf(colType) == -1) {
				colTypeSb.append('(');
				colTypeSb.append(rs.getString("COLUMN_SIZE"));
				if (decimalDigits != null && !decimalDigits.equals("0")) {
					colTypeSb.append(",");
					colTypeSb.append(decimalDigits);
				}
				colTypeSb.append(')');
			} // fixSizeType

			colsMap.put(rs.getString("COLUMN_NAME"), colTypeSb.toString());
		}
		// System.out.println(colsMap.toString());
		conn.close();

		return colsMap;
	}

	/**
	 * map keys
	 */
	private static Map<String, Boolean> mapKeys(Map<String, String> colsMap,
			String keys) {

		Map<String, Boolean> colsIsKey = new LinkedHashMap<String, Boolean>();

		for (String col : colsMap.keySet()) {
			if (keys.indexOf(col.toUpperCase()) != -1) {
				colsIsKey.put(col, Boolean.TRUE);
			} else {
				colsIsKey.put(col, Boolean.FALSE);
			}
		}
		return colsIsKey;
	}

	/**
	 * Run template
	 */
	private static void runTemplate(String template, VelocityEngine ve, VelocityContext context) {

		/* next, get the Template */
		Template t = ve.getTemplate(template);
		/* now render the template into a StringWriter */
		StringWriter writer = new StringWriter();
		t.merge(context, writer);

		/* show the World */
		System.out.println(writer.toString());

	}

	/**
	 * Map table column types to HAWQ types
	 * 
	 * @param type
	 *            from db metadata
	 * @return hawq type
	 */
	private static String typeMap(String type) {
		String newType = typeMap.get(type);
		return newType != null ? newType : type;
	}

	/**
	 * Load properties files in classpath into Map
	 * 
	 * @param name
	 * @param map
	 * @throws IOException
	 */
	private static void loadProps(String name, Map<String, String> map)
			throws IOException {
		Properties prop = new Properties();
		InputStream in = prop.getClass().getResourceAsStream(name);
		prop.load(in);
		in.close();

		for (final String key : prop.stringPropertyNames()) {
			map.put(key, prop.getProperty(key));
		}
	}

	/*
	 * Static initialization
	 */
	static {

		typeMap = new HashMap<String, String>() {
			private static final long serialVersionUID = 1L;
			{
				put("INT8", "BIGINT");
			}
		};

		tablePk = new HashMap<String, String>();
		appProps = new HashMap<String, String>();
		try {
			loadProps("/tables.properties", tablePk);
			loadProps("/application.properties", appProps);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
