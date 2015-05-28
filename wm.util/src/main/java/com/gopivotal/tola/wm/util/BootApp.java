package com.gopivotal.tola.wm.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.gopivotal.tola.wm.util.service.TemplateService;

/**
 * Delta generation
 *
 */
@SpringBootApplication
public class BootApp implements CommandLineRunner
{
	private final static Logger logger = LoggerFactory.getLogger(BootApp.class); 


	@Autowired
	private TemplateService templateService;


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
		for (String tableName : templateService.getTables()) {
			
			if (argTablename != null && !argTablename.equalsIgnoreCase(tableName)) {
				continue;
			}
			
			templateService.assignContext(tableName);
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
