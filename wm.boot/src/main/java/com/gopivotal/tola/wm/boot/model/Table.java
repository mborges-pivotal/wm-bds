/*=========================================================================
 * Copyright (c) 2010-2014 Pivotal Software, Inc. All rights reserved.
 * This product is protected by U.S. and international copyright
 * and intellectual property laws. Pivotal products are covered by
 * one or more patents listed at http://www.gopivotal.com/patents.
 *=========================================================================
 */
package com.gopivotal.tola.wm.boot.model;

import java.util.ArrayList;
import java.util.List;

public class Table {
	
	public String catalog;
	public String schema;
	public String name;
	public String type;
	
	List<Batch> batches = new ArrayList<Batch>();
	
	public Table(String catalog, String schema, String name, String type) {
		this.catalog = catalog;
		this.schema = schema;
		this.name = name;
	}
	
	public void addBatch(Batch b) {
		batches.add(b);
	}

}
