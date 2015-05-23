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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
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
import java.util.TreeMap;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates ELT artifacts
 * 
 * @see http
 *      ://www.javaworld.com/article/2075966/core-java/start-up-the-velocity-
 *      template-engine.html
 * 
 * @author mborges
 * 
 * @deprecated - Converted into BootApp
 *
 */
public class Main {
	
	private final static Logger logger = LoggerFactory.getLogger(Main.class); 

	private static Map<String, String> typeMap = null;
	private static Map<String, String> tablePk = null;
	private static Map<String, String> appProps = null;

	/**
	 * Main - generate delta scripts
	 * 
	 * @param args - T=tables, L=load, C=clear, A=audit, D=All (default)
	 * @throws IOException 
	 */
	public static void main2(String[] args) throws IOException {

		logger.info("JDBC Properties '{}'",appProps);
		logger.info("Tables '{}'", tablePk.keySet());

		String argTablename = null;
		char arg = 'D';
		if (args.length > 0) {
			arg = args[0].toUpperCase().charAt(0);
			if (args.length > 1) {
				argTablename = args[1];
				logger.info("Generating script for '{}' table",argTablename);
			}
		}
		logger.info("Generation '{}' type",arg);

		/* first, get and initialize an engine */
		VelocityEngine ve = new VelocityEngine();
		ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		ve.setProperty("classpath.resource.loader.class",
				ClasspathResourceLoader.class.getName());

		ve.init();

		FileWriter wClear = new FileWriter("target/clear.sql");
		FileWriter wAudit = new FileWriter("target/audit.sql");
		FileWriter wLoad = new FileWriter("target/load.sql");
		FileWriter wTables = new FileWriter("target/tables.sql");
				
		// Loop to all the tables
		for (String tableName : tablePk.keySet()) {
			
			if (argTablename != null && !argTablename.equalsIgnoreCase(tableName)) {
				continue;
			}
			
			logger.info("Processing '{}' table",tableName);

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
			context.put("address", String.format("%s:%s", appProps.get("gpfdist_hostname"),appProps.get("gpfdist_port")));

			switch(arg) {
			case 'A':
				runTemplate(wAudit, "audit.vm", ve, context);
				break;
			case 'C':
				runTemplate(wClear, "clear.vm", ve, context);
				break;
			case 'T':
				runTemplate(wTables, "tables.vm", ve, context);
				break;
			case 'L':
				runTemplate(wLoad, "load.vm", ve, context);
				break;
			default:
				runTemplate(wClear, "clear.vm", ve, context);
				runTemplate(wTables, "tables.vm", ve, context);
				runTemplate(wLoad, "load.vm", ve, context);
				runTemplate(wAudit, "audit.vm", ve, context);
			}

		} // for each table
		
		wAudit.close();
		wClear.close();
		wLoad.close();
		wTables.close();
		
		logger.info("Files generated.");
		
	} // main


	/**
	 * get Table information
	 * 
	 * TODO: Use pg_catalog to get distribution and other information
	 * 
	 * @throws SQLException
	 */
	private static Map<String, String> getTable(String tableName)
			throws SQLException {

		String fixedSizeType = appProps.get("fixed_size_type");

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

			if (fixedSizeType.indexOf(colType) == -1) {
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
	private static void runTemplate(Writer writer, String template, VelocityEngine ve, VelocityContext context) {
		Template t = ve.getTemplate(template);		
		t.merge(context, writer);
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

		tablePk = new TreeMap<String, String>();
		appProps = new HashMap<String, String>();
		try {
			loadProps("/tables.properties", tablePk);
			loadProps("/application.properties", appProps);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
