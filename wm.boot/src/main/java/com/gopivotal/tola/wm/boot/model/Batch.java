/*=========================================================================
 * Copyright (c) 2010-2014 Pivotal Software, Inc. All rights reserved.
 * This product is protected by U.S. and international copyright
 * and intellectual property laws. Pivotal products are covered by
 * one or more patents listed at http://www.gopivotal.com/patents.
 *=========================================================================
 */
package com.gopivotal.tola.wm.boot.model;

import java.util.Date;

public class Batch {
	
	public String tableName;
	public long id;
	public Date date;
	public long count;
	public long deleted;
	public long updated;
	public long inserted;
	
	public Batch(String tableName, long id, Date date, long count, long deleted, long updated, long inserted) {
		this.tableName = tableName;
		this.id = id;
		this.date = date;
		this.count = count;
		this.deleted = deleted;
		this.updated = updated;
		this.inserted = inserted;
	}

}
