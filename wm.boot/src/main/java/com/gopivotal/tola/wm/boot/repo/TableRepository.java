/*=========================================================================
 * Copyright (c) 2010-2014 Pivotal Software, Inc. All rights reserved.
 * This product is protected by U.S. and international copyright
 * and intellectual property laws. Pivotal products are covered by
 * one or more patents listed at http://www.gopivotal.com/patents.
 *=========================================================================
 */
package com.gopivotal.tola.wm.boot.repo;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.stereotype.Component;

import scala.annotation.meta.param;

import com.gopivotal.tola.wm.boot.model.LoadError;
import com.gopivotal.tola.wm.boot.model.Batch;
import com.gopivotal.tola.wm.boot.model.BatchDetail;
import com.gopivotal.tola.wm.boot.model.Table;
import com.gopivotal.tola.wm.util.service.TemplateService;
import com.gopivotal.tola.wm.util.service.UtilService;

/**
 * TableRepository
 * 
 * @author mborges
 *
 */
@Component
public class TableRepository {

	@Autowired
	private JdbcTemplate jdbc;
	
	@Autowired
	private TemplateService templateService;
	
	private Map<String, Map<String,String>> dmlCache = new HashMap<String, Map<String,String>>();
	
	//ERRORS
	
	//BATCH RECORDS

	@SuppressWarnings("unchecked")
	public List<Table> getTables() {
		GetTableNames getTableNames = new GetTableNames();
		try {
			return ((List<Table>) JdbcUtils.extractDatabaseMetaData(
					jdbc.getDataSource(), getTableNames));

		} catch (MetaDataAccessException e) {
			System.out.println(e);
		}
		throw new java.lang.Error("Failed to retrieve tables");
	}

	/**
	 * getErrors
	 * @return
	 */
	public List<LoadError> getErrors() {
		
		String sql = String.format("select to_char(cmdtime, 'DD Mon YYYY HH24:MI:SS') as cmdtime,count(*),filename from err_wm group by cmdtime,filename");
		
		List<LoadError> errors = new ArrayList<LoadError>();
		List<Map<String, Object>> rows = jdbc.queryForList(sql);
		for (Map<String, Object> row : rows) {
			LoadError error = new LoadError((String)row.get("cmdtime"), (Long) row.get("count"), (String)row.get("filename"));
			errors.add(error);
		}

		return errors;
	}
	
	
	/**
	 * getBatches - all batch runs for a table
	 * @param table
	 * @return
	 */
	public List<Batch> getBatches(String table) {
		
		String sql = String.format("SELECT DISTINCT batch_id, COUNT(*), load_date FROM %s group by batch_id, load_date", table);
		
		List<Batch> batches = new ArrayList<Batch>();
		List<Map<String, Object>> rows = jdbc.queryForList(sql);
		for (Map<String, Object> row : rows) {
			Batch batch = new Batch(table, (Long) row.get("batch_id"),
					(Date) row.get("load_date"), (Long) row.get("count"));
			batches.add(batch);
		}

		return batches;
	}

	/**
	 * getBatch - stats for a batch run
	 * @param table
	 * @param batchIdPrior
	 * @param batchIdCurrent
	 * @return
	 */
	public BatchDetail getBatch(String table, int batchIdPrior, int batchIdCurrent ) {
		
		// hack
		if (table.toUpperCase().endsWith("_AO")) {
			table = table.substring(0,table.length()-3);
		}
		
		String sql = getQuery(table, "COUNT_DIFFERENCES");
		
		List<Map<String, Object>> rows = jdbc.queryForList(sql, batchIdPrior, batchIdCurrent);
		
		if (rows.size() <= 0) {
			return null;
		}
		
		Map<String, Object> row = rows.get(0);
		BatchDetail batch = new BatchDetail(table, (Long) row.get("batch_id"),
					(Date) row.get("load_date"), (Long) row.get("count"),
					(Long) row.get("deleted"), (Long) row.get("updated"),
					(Long) row.get("inserted"));
		return batch;
	}
	
	////////////////////////////////////////////////
	
	/**
	 * getDML for table audit
	 * 
	 * @param tableName
	 * @return
	 */
	private String getQuery(String tableName, String queryName) {
		
		Map<String, String> tblQueries = dmlCache.get(tableName);
		if(tblQueries == null) {
			Writer w = new StringWriter();
			templateService.assignContext(tableName);
			templateService.runTemplate(w, "audit.vm");		
			tblQueries = templateService.parseQueries(w.toString());
			dmlCache.put(tableName, tblQueries);
		}
		
		if (tblQueries.containsKey(queryName)) {
			return tblQueries.get(queryName);
		}
		
		throw new Error(String.format("Didn't find query '%s' for table '%s'", queryName, tableName));
	}
	

}
