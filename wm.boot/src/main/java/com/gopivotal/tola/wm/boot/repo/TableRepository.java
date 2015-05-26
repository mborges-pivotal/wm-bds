/*=========================================================================
 * Copyright (c) 2010-2014 Pivotal Software, Inc. All rights reserved.
 * This product is protected by U.S. and international copyright
 * and intellectual property laws. Pivotal products are covered by
 * one or more patents listed at http://www.gopivotal.com/patents.
 *=========================================================================
 */
package com.gopivotal.tola.wm.boot.repo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.stereotype.Component;

import com.gopivotal.tola.wm.boot.model.LoadError;
import com.gopivotal.tola.wm.boot.model.Batch;
import com.gopivotal.tola.wm.boot.model.BatchDetail;
import com.gopivotal.tola.wm.boot.model.Table;

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
		List<Map<String, Object>> rows = jdbc.queryForList(sql, batchIdPrior, batchIdCurrent);
		Map<String, Object> row = rows.get(0);
		BatchDetail batch = new BatchDetail(table, (Long) row.get("batch_id"),
					(Date) row.get("load_date"), (Long) row.get("count"),
					(Long) row.get("deleted"), (Long) row.get("updated"),
					(Long) row.get("inserted"));
		return batch;
	}
	
	private static String sql = "SELECT batch_id,"
			+ "   load_date,"
			+ "       count(*),"
			+ "       sum(deleted::int) as deleted, "
			+ "       sum(changed::int) as updated, "
			+ "       sum(inserted::int) as inserted "
			+ "FROM ("
			+ "       SELECT coalesce(a.relative_record_id,b.relative_record_id) as relative_record_id,coalesce(a.library_nm,b.library_nm) as library_nm,coalesce(a.network_addr,b.network_addr) as network_addr,coalesce(a.location_nm,b.location_nm) as location_nm,"
			+ "              deleted, "
			+ "              (non_key_hash not similar to md5) as changed,"
			+ "              (md5 is null) as inserted,"
			+ "              load_date,"
			+ "              batch_id "
			+ "       FROM ar_cmpny_ao a "
			+ "       FULL OUTER JOIN ("
			+ "                         SELECT  relative_record_id, library_nm, network_addr, location_nm,"
			+ "                                non_key_hash as md5"
			+ "                         FROM ar_cmpny_ao"
			+ "                         WHERE batch_id = ?"
			+ // :batch_id_prior
			"                       ) b "
			+ "       ON (a.relative_record_id=b.relative_record_id and a.library_nm=b.library_nm and a.network_addr=b.network_addr and a.location_nm=b.location_nm) "
			+ "       WHERE batch_id = ?"
			+ // :batch_id_curr
			"     ) " + "AS changes " + "WHERE deleted or inserted or changed "
			+ "GROUP BY batch_id, load_date;";

}
