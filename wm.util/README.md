# Delta Generation Script
This program generates the necessary scripts to implement the delta process described in the parent project. The program uses JDBC Database Metadata to build the scripts based on an existing Table. 

Currently the program relies on 2 properties files located in the */src/main/resources* folder:

* application.properties - Connection information for the JDBC source. The JDBC driver must be located in the CLASSPATH
* tables.properties - Properties files with tablename and columns representing the key (record uniqueness)

The program will output the scripts for all tables in the *table.properties* file. The scripts are created with Velocity templates for clearing (dropping) the tables, creating the tables and inserting the deltas:

* clear.vm
* tables.vm
* load.vm

Both tables.vm and load.vm shared the macro.vm template with some help functions. The template parameters are built based on the program input (tables.properties or argument). The template parameters were defined in such way so it can be easily tested standalone.

```
#set ($tablename = "DYNAMIC_BASKET")
#set ($cols_isKey = {"EVENT_ID":true, "BASKET_ID":true, "INSTANCE":true, "IMPACTED_AMOUNT":false, ... "CALL_START":false})
#set ($cols_types = {"EVENT_ID":"DECIMAL(38)", "BASKET_ID":"VARCHAR(8)", "INSTANCE":"INTEGER", ... "CALL_START":"TIMESTAMP"})
```
Optionally, the program can take 2 parameters:

* T=tables, L=load, C=clear, A=all (default) - So you can generate either script
* tablename - The tables.properties files is still used, but only the scripts for the provided tablename are generated

##Running

1. Run [gpfdist](http://hawq.docs.pivotal.io/docs-gpdb/utility_guide/admin_utilities/gpfdist.html#topic1) on the edge node where the CSV files are located.
  * gpfdist -p 7171 - using port 7171 (can be changed in the tables.vm template)
2. Run the load.vm generate scripts
 



