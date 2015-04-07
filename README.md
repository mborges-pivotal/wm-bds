# wm-bds
This project demonstrates how to batch load table differences from daily table dumps. This is not a replacement for Change Data Capture (CDC) products, but an alternate way when CDC products are not available for the source.

The implementation is based on Pivotal Greenplum Database (GPDB) and its Hadoop brother HAWQ. In GPDB we have additional capabilities that are not explored in this project yet. For example, the ability to update and delete records which is not supported in HAWQ.

In this project you'll find 4 files used in the demonstration:

* dynamic_basket.csv - this is used for the initial load
* dynamic_basket_daily_1.csv - this is for the first daily load
* dynamic_basket_daily_2.csv - this is for the second daily load
* hawq-ddl.sql - DDL and DML 

The assumption is that the daily data sets are provided as CSV files. We use 2 external tables for loading the data. We'll use gpfdist, but for now we simply use PXF. The main table with the data is an append-only table called *dynamic_table_ao*. This table adds 4 controls columns explained below:

* deleted - boolean used for logical deletes (remember HAWQ doesn't have update or delete)
* insert_id - used for versioning of the record (hawq doesn't support index, so no primary key)
* non_key_hash - md5(64) Hash of the table values used for easy comparision of records
* load_date - used for historical purpose only

In order to simplifly the use of the table, we have created the view, *dynamic_basket*, that hides old rows and the control fields.

## How it works
The idea is quite simple. We do a full join between the view and the external table checking the md5 hash result of the values and with 3 simple assumption we can infer deletes, updates and inserts. We join based on the columns that represent an unique row or a primary key.

Here are the checks:

1. md5 of the external table is not present, then the row was deleted. This means they primary key was not found in the external table.
2. md5 of view is different than external table, then the row was updated.
3. md5 of the view is not present, then the row was inserted

We use the result of this query to insert into the table. 

## Running

1. Look at the *hawq_ddl.sql* for the 2 external tables and write down the location of the CSV files in HDFS
..1. Move the csv files to the location
2. Run the *hawq_ddl.sql*. This script will create all the tables and views and run the load SQLs. 

**NOTE:** I'll add more detail explanation, but if you look at the *hawq_ddl.sql* you'll see how it works. 

