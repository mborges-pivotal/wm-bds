/*=========================================================================
 * Copyright (c) 2010-2014 Pivotal Software, Inc. All rights reserved.
 * This product is protected by U.S. and international copyright
 * and intellectual property laws. Pivotal products are covered by
 * one or more patents listed at http://www.gopivotal.com/patents.
 *=========================================================================
 */
package com.gopivotal.tola.wm.boot.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gopivotal.tola.wm.boot.model.Batch;
import com.gopivotal.tola.wm.boot.model.Table;
import com.gopivotal.tola.wm.boot.repo.TableRepository;

/**
 * DetalController - methods for managing batch loads and deltas
 * 
 * @author mborges
 *
 */
@RestController
public class DeltaController {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private TableRepository tables;
	
	@RequestMapping("/")
	public String index() {
		return "Greetings from WM Boot!";
	}
	
	@RequestMapping("batch")
	public List<Batch> getBatch(@RequestParam("table") String table, @RequestParam("prior") int batchIdPrior, @RequestParam("curr") int batchIdCurrent) {
		logger.info("Get batches for table {} prior {} current {}", table, batchIdPrior, batchIdCurrent);
		return tables.getBatches(table, batchIdPrior, batchIdCurrent);
	}

	@RequestMapping("tables")
	public List<Table> getTables() {
		logger.info("Get tables");
		return tables.getTables();
	}

}
