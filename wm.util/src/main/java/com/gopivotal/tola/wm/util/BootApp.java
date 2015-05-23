package com.gopivotal.tola.wm.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import com.gopivotal.tola.wm.util.service.TableService;
import com.gopivotal.tola.wm.util.service.TemplateService;
import com.gopivotal.tola.wm.util.service.UtilService;

/**
 * Delta generation
 *
 */
@SpringBootApplication
public class BootApp implements CommandLineRunner
{
	private final static Logger logger = LoggerFactory.getLogger(BootApp.class); 
	
	private Map<String, String> tablePk = null;

	@Autowired
	private UtilService utilService;

	@Autowired
	private TableService tableService;

	@Autowired
	private TemplateService templateService;

	@Value("${gpfdist_hostname}") 
	private String gpfdistHostname;

	@Value("${gpfdist_port}") 
	private String gpfdistPort;
	
	/**
	 * main - Spring Boot application 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		SpringApplication.run(BootApp.class, args);
	}
	
	/**
	 * CommandLineRunner
	 */
	public void run(String... args) {
		
		tablePk = new TreeMap<String, String>(); 
		
		// Load list of tables with key definitions
		try {
			utilService.loadProps("/tables.properties", tablePk);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		logger.info("Tables '{}'", tablePk.keySet());

		String argTablename = null;
		
		// Parsing args
		char arg = 'D';
		if (args.length > 0) {
			arg = args[0].toUpperCase().charAt(0);
			if (args.length > 1) {
				argTablename = args[1];
				logger.info("Generating script for '{}' table",argTablename);
			}
		}
		logger.info("Generation '{}' type",arg);
		
		logger.info("gpfdist host {} and port {}",gpfdistHostname, gpfdistPort);

		
		// Looping through defined tables and processing scripts
		FileWriter wClear = null;
		FileWriter wAudit = null;
		FileWriter wLoad = null;
		FileWriter wTables = null;
		try {
			wClear = new FileWriter("target/clear.sql");
			wAudit = new FileWriter("target/audit.sql");
			wLoad = new FileWriter("target/load.sql");
			wTables = new FileWriter("target/tables.sql");
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		// Loop to all the tables
		for (String tableName : tablePk.keySet()) {
			
			if (argTablename != null && !argTablename.equalsIgnoreCase(tableName)) {
				continue;
			}
			
			assignContext(tableName);
			processTemplates(arg, wAudit, wClear, wTables, wLoad);
			
			
		} // for each table
					
		try {
			wAudit.close();
			wClear.close();
			wLoad.close();
			wTables.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		logger.info("Files generated.");		
	}
	
	///////////////////////////////////
	// Helper methods
	///////////////////////////////////
	
	/**
	 * assign the context for the generation
	 * @param tableName
	 */
	private void assignContext(String tableName) {
		logger.info("Processing '{}' table",tableName);

		Map<String, String> colsMap = null;
		Map<String, Boolean> colsIsKey = null;

		/* Catalog */
		try {
			colsMap = tableService.getTable(tableName);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		colsIsKey = tableService.mapKeys(colsMap,tablePk.get(tableName));

		templateService.addContext("tablename", tableName);
		templateService.addContext("cols_isKey", colsIsKey);
		templateService.addContext("cols_types", colsMap);
		templateService.addContext("address", String.format("%s:%s", gpfdistHostname,gpfdistPort));
		
	}
	
	private void processTemplates(char type, Writer wAudit, Writer wClear, Writer wTables, Writer wLoad) {
		switch(type) {
		case 'A':
			templateService.runTemplate(wAudit, "audit.vm");
			break;
		case 'C':
			templateService.runTemplate(wClear, "clear.vm");
			break;
		case 'T':
			templateService.runTemplate(wTables, "tables.vm");
			break;
		case 'L':
			templateService.runTemplate(wLoad, "load.vm");
			break;
		default:
			templateService.runTemplate(wClear, "clear.vm");
			templateService.runTemplate(wTables, "tables.vm");
			templateService.runTemplate(wLoad, "load.vm");
			templateService.runTemplate(wAudit, "audit.vm");
		}
		
	}

}
