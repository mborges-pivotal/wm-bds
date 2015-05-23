/*=========================================================================
 * Copyright (c) 2010-2014 Pivotal Software, Inc. All rights reserved.
 * This product is protected by U.S. and international copyright
 * and intellectual property laws. Pivotal products are covered by
 * one or more patents listed at http://www.gopivotal.com/patents.
 *=========================================================================
 */
package com.gopivotal.tola.wm.util.service;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.stereotype.Component;

/**
 * TableService - get table metadata information to run templates
 * 
 * @author mborges
 *
 */
@Component
public class TableService {

	@Autowired
	private JdbcTemplate jdbc;
	
	@Value("${fixed_size_type}") 
	private String fixedSizeType;

	/**
	 * get Table information
	 * 
	 * TODO: Use pg_catalog to get distribution and other information
	 * 
	 * @throws SQLException
	 */
	@SuppressWarnings("unchecked")
	public Map<String, String> getTable(String tableName) throws SQLException {
		GetTableMetadata getTableMetadata = new GetTableMetadata(tableName, fixedSizeType);
		try {
			return (Map<String, String>) JdbcUtils.extractDatabaseMetaData(
					jdbc.getDataSource(), getTableMetadata);

		} catch (MetaDataAccessException e) {
			System.out.println(e);
		}
		throw new Error("Failed to retrieve tables");
	}
	
	/**
	 * mapKeys - Creates a Map with Table columns indicating which are keys
	 * 
	 * @param colsMap - map of columns from Database Metadata - see GetTable
	 * @param keys - list of keys for a Table
	 * @return Map<String, Boolean> Column -> True if key else False
	 */
	public Map<String, Boolean> mapKeys(Map<String, String> colsMap, String keys) {

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

}
