/*=========================================================================
 * Copyright (c) 2010-2014 Pivotal Software, Inc. All rights reserved.
 * This product is protected by U.S. and international copyright
 * and intellectual property laws. Pivotal products are covered by
 * one or more patents listed at http://www.gopivotal.com/patents.
 *=========================================================================
 */
package com.gopivotal.tola.wm.util.service;

import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.PostConstruct;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * TemplateService - run templates for delta process
 * 
 * @author mborges
 *
 */
@Component
public class TemplateService {
	
	private final static Logger logger = LoggerFactory.getLogger(TemplateService.class); 
	
	private Map<String, String> tablePk = null;

	private VelocityEngine ve = new VelocityEngine();
	private VelocityContext context = new VelocityContext();
	
	@Autowired
	private UtilService utilService;	
	
	@Autowired
	private TableService tableService;
	
	@Value("${gpfdist_hostname}") 
	private String gpfdistHostname;

	@Value("${gpfdist_port}") 
	private String gpfdistPort;	

	/**
	 * constructor
	 */
	public TemplateService() {
		ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		ve.setProperty("classpath.resource.loader.class",
				ClasspathResourceLoader.class.getName());
		ve.init();		
		
	}
	
	@PostConstruct
	public void init() {
		loadTable();
	}
		
	/**
	 * Run template
	 */
	public void runTemplate(Writer writer, String template) {

		Template t = ve.getTemplate(template);		
		t.merge(context, writer);
	}

	/**
	 * assign the context for the generation
	 * @param tableName
	 */
	public void assignContext(String tableName) {
		logger.info("Processing '{}' table",tableName);
		
		logger.info("gpfdist host {} and port {}",gpfdistHostname, gpfdistPort);
	

		Map<String, String> colsMap = null;
		Map<String, Boolean> colsIsKey = null;

		/* Catalog */
		try {
			colsMap = tableService.getTable(tableName);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		colsIsKey = tableService.mapKeys(colsMap,tablePk.get(tableName));

		context.put("tablename", tableName);
		context.put("cols_isKey", colsIsKey);
		context.put("cols_types", colsMap);
		context.put("address", String.format("%s:%s", gpfdistHostname,gpfdistPort));
		
	}
	
	/**
	 * getTable 
	 * @return Use the tables.properties with list of columns that make up the table key
	 */
	public Set<String> getTables() {
		return tablePk.keySet();
	}
	
	private void loadTable() {
		
		if (tablePk == null) {
			tablePk = new TreeMap<String, String>(); 
		} else {
			return;
		}
		
		// Load list of tables with key definitions
		try {
			utilService.loadProps("/tables.properties", tablePk);
		} catch (IOException e) {
			e.printStackTrace();
		}		
		
		logger.info("Tables '{}'", tablePk.keySet());
			
	}
	/**
	 * parseQueries - parse a template generated buffer for queries
	 * It relies on comments to detect the boundaries: -- NAME and -- END
	 * @param buffer
	 * @return
	 */
	private static final int ST_OPEN = 0;
	private static final int ST_START = 1;
	private static final int ST_QUERY = 2;
	private static final int ST_END = 3;
	
	public Map<String, String> parseQueries(String buffer) {
		
		Map<String, String> queries = new HashMap<String, String>();
		
		String[] lines = buffer.split(System.getProperty("line.separator"));
		int state = ST_OPEN;
		StringBuffer querySb = new StringBuffer();
		String queryName = null;
		for(String line: lines) {
			
			switch(state) {
			case ST_OPEN:
				if (line.startsWith("--")) {
					state = ST_START;
				}
				break;
			case ST_START:
				if (line.matches("-- (\\w+)")) {
					state = ST_QUERY;
					queryName = line.substring(3);
				}
				break;
			case ST_QUERY:
				
				line = line.replaceAll(" (:\\w+)", " ?");
				
				if (line.matches("-- END")) {
					state = ST_END;
					System.out.printf("%s -> %s\n", queryName, querySb.toString());
					queries.put(queryName, querySb.toString());
				} else if (!line.startsWith("--")) {
					querySb.append(line);
					querySb.append("\n");
				}
				break;
			case ST_END:
				queryName = null;
				querySb.setLength(0);
				state = ST_OPEN;
				break;
			} //switch() 
			
		} // for
		
		return queries;
	}
	
	////////// TURN INTO A TEST
	
	public static void main2(String[] args) {
		TemplateService s = new TemplateService();
		s.parseQueries(output);
		
		if ("       WHERE batch_id = :batch_id_curr".matches(".* (:\\w+)")) {
			System.out.println("matches");
		} else {
			System.out.println("NOT");
			
		}		

	}
	
	static String output = "\n" + 
			"--\n" + 
			"-- LIST_BATCHES\n" + 
			"--\n" + 
			"SELECT DISTINCT batch_id, COUNT(*), load_date \n" + 
			"FROM ar_cmpny_ao group by batch_id, load_date;\n" + 
			"-- END\n" + 
			"\n" + 
			"\n" + 
			"--\\set batch_id_curr 2\n" + 
			"--\\set batch_id_prior 1\n" + 
			"\n" + 
			"--\n" + 
			"-- COUNT_DIFFERENCES\n" + 
			"--\n" + 
			"SELECT batch_id,\n" + 
			"       load_date,\n" + 
			"       count(*),\n" + 
			"       sum(deleted::int) as deleted, \n" + 
			"       sum(changed::int) as changed, \n" + 
			"       sum(inserted::int) as inserted\n" + 
			"FROM (  \n" + 
			"       SELECT coalesce(a.relative_record_id,b.relative_record_id) as relative_record_id,coalesce(a.library_nm,b.library_nm) as library_nm,coalesce(a.network_addr,b.network_addr) as network_addr,coalesce(a.location_nm,b.location_nm) as location_nm,\n" + 
			"              deleted, \n" + 
			"              (non_key_hash not similar to md5) as changed, \n" + 
			"              (md5 is null) as inserted, \n" + 
			"              load_date, \n" + 
			"              batch_id\n" + 
			"       FROM ar_cmpny_ao a \n" + 
			"       FULL OUTER JOIN (\n" + 
			"                         SELECT  relative_record_id, library_nm, network_addr, location_nm, \n" + 
			"                                non_key_hash as md5\n" + 
			"                         FROM ar_cmpny_ao\n" + 
			"                         WHERE batch_id = :batch_id_prior\n" + 
			"                       ) b \n" + 
			"       ON (a.relative_record_id=b.relative_record_id and a.library_nm=b.library_nm and a.network_addr=b.network_addr and a.location_nm=b.location_nm) \n" + 
			"       WHERE batch_id = :batch_id_curr\n" + 
			"     ) \n" + 
			"AS changes\n" + 
			"WHERE deleted or inserted or changed\n" + 
			"GROUP BY batch_id, load_date;\n" + 
			"-- END\n" + 
			"\n" + 
			"--\n" + 
			"-- SHOW_DIFFERENCES\n" + 
			"--\n" + 
			"SELECT batch_id,\n" + 
			"       load_date,\n" + 
			"        relative_record_id, library_nm, network_addr, location_nm, \n" + 
			"       deleted, \n" + 
			"       changed, \n" + 
			"       inserted\n" + 
			"FROM (  \n" + 
			"       SELECT coalesce(a.relative_record_id,b.relative_record_id) as relative_record_id,coalesce(a.library_nm,b.library_nm) as library_nm,coalesce(a.network_addr,b.network_addr) as network_addr,coalesce(a.location_nm,b.location_nm) as location_nm,\n" + 
			"              deleted, \n" + 
			"              (non_key_hash not similar to md5) as changed, \n" + 
			"              (md5 is null) as inserted,  \n" + 
			"              load_date, \n" + 
			"              batch_id\n" + 
			"       FROM ar_cmpny_ao a \n" + 
			"       FULL OUTER JOIN (\n" + 
			"                         SELECT  relative_record_id, library_nm, network_addr, location_nm, \n" + 
			"                                non_key_hash as md5\n" + 
			"                         FROM ar_cmpny_ao\n" + 
			"                         WHERE batch_id = :batch_id_prior\n" + 
			"                       ) b \n" + 
			"       ON (a.relative_record_id=b.relative_record_id and a.library_nm=b.library_nm and a.network_addr=b.network_addr and a.location_nm=b.location_nm) \n" + 
			"       WHERE batch_id = :batch_id_curr\n" + 
			"     ) \n" + 
			"AS changes\n" + 
			"WHERE deleted or inserted or changed;\n" + 
			"-- END\n" + 
			"\n" + 
			"\n" + 
			"\n" + 
			"";
	
	
	

}
