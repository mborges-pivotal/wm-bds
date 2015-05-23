/*=========================================================================
 * Copyright (c) 2010-2014 Pivotal Software, Inc. All rights reserved.
 * This product is protected by U.S. and international copyright
 * and intellectual property laws. Pivotal products are covered by
 * one or more patents listed at http://www.gopivotal.com/patents.
 *=========================================================================
 */
package com.gopivotal.tola.wm.util.service;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.support.DatabaseMetaDataCallback;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.stereotype.Component;

import com.gopivotal.tola.wm.util.BootApp;


/**
 * GetTableMetadata - DatabaseMetaDataCallback for JdbcTemplate
 * 
 * @author mborges
 *
 */
public class GetTableMetadata implements DatabaseMetaDataCallback {

	private final static Logger logger = LoggerFactory.getLogger(GetTableMetadata.class); 
	
	private String fixedSizeType;
	private String tableName;
	private Map<String, String> typeMap = null;
	
	/**
	 * Constructor
	 * @param tableName
	 */
	public GetTableMetadata(String tableName, String fixedSizeType) {
		this.tableName = tableName;
		this.fixedSizeType = fixedSizeType;
		
        // TODO: move to properties file
		typeMap = new HashMap<String, String>() {
			private static final long serialVersionUID = 1L;
			{
				put("INT8", "BIGINT");
			}
		};		

	}
	
	/**
	 * processMetaData
	 */
	public Object processMetaData(DatabaseMetaData dbmd) throws SQLException,
			MetaDataAccessException {
		ResultSet rs = dbmd.getColumns(null, null, tableName, null); // catalog,
		// schemaPattern,
		// tableNamePattern,
		// types)

		Map<String, String> colsMap = new LinkedHashMap<String, String>();
		while (rs.next()) {
			String decimalDigits = rs.getString("DECIMAL_DIGITS");
			String colType = mapType(rs.getString("TYPE_NAME").toUpperCase());
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

		return colsMap;
	}
	
	////////////////////////////
	// Helper methods	
	////////////////////////////
	
	/**
	 * Map table column types to HAWQ types
	 * 
	 * @param type
	 *            from db metadata
	 * @return hawq type
	 */
	private String mapType(String type) {
		String newType = typeMap.get(type);
		return newType != null ? newType : type;
	}	

}
