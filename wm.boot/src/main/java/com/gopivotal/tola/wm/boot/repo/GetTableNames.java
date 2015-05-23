/*=========================================================================
 * Copyright (c) 2010-2014 Pivotal Software, Inc. All rights reserved.
 * This product is protected by U.S. and international copyright
 * and intellectual property laws. Pivotal products are covered by
 * one or more patents listed at http://www.gopivotal.com/patents.
 *=========================================================================
 */
package com.gopivotal.tola.wm.boot.repo;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.support.DatabaseMetaDataCallback;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.stereotype.Component;

import com.gopivotal.tola.wm.boot.model.Table;

@Component
public class GetTableNames implements DatabaseMetaDataCallback {
	
	@Value("${app.catalog}")
	private String catalog;
	
	@Value("${app.schema}")
	private String schema;

	@Override
	public Object processMetaData(DatabaseMetaData dbmd) throws SQLException,
			MetaDataAccessException {
		 ResultSet rs  = dbmd.getTables(catalog,schema,"%",new String[]{"TABLE"});
         List<Table> l = new ArrayList<Table>();
         while(rs.next()) {
         	l.add(new Table(catalog, schema, rs.getString(3), rs.getString(4)));
         }
         return l;
	}

}
