-- 
-- DROP TABLE
--
DROP FUNCTION load_ext_1();
DROP EXTERNAL TABLE IF EXISTS DYNAMIC_BASKET_EXT;
DROP EXTERNAL TABLE IF EXISTS DYNAMIC_BASKET_EXT2;

DROP TABLE IF EXISTS DYNAMIC_BASKET_AO CASCADE;

--
-- USED TO LOAD DATA FROM EXTERNAL TABLE
--
CREATE OR REPLACE FUNCTION load_ext_1() 
  RETURNS TEXT AS
  $func$ 
  DECLARE
    tablename varchar := 'dynamic_basket';
    keys varchar := 'BASKET_ID, INSTANCE, EVENT_ID';
    cols varchar := 'IMPACTED_AMOUNT, EXPIRATION_DATE, REMAINING_AMOUNT, OFFER_CODE, CALL_START';
    ctl_cols varchar := 'NON_KEY_HASH, LOAD_DATE'; 
    all_cols varchar := keys || ',' || cols;
    md5 varchar := 'md5((IMPACTED_AMOUNT||EXPIRATION_DATE||REMAINING_AMOUNT||OFFER_CODE||CALL_START)::text)';
    sql text := 'INSERT INTO ' || tablename || '_ao ('|| all_cols || ',' || ctl_cols || ') 
                 SELECT ' || all_cols || ',' || md5 || ', current_date FROM ' ||  tablename || '_ext';
  BEGIN
    EXECUTE sql;
    RETURN 'COUNT xxx'; 
  END
  $func$
  LANGUAGE plpgsql;

--
-- DYNAMIC_BASKET_EXT - First load
--
CREATE EXTERNAL TABLE DYNAMIC_BASKET_EXT
   (
      IMPACTED_AMOUNT  DECIMAL(15,6),
      EXPIRATION_DATE  TIMESTAMP,
      REMAINING_AMOUNT DECIMAL,
      BASKET_ID        VARCHAR(8),
      INSTANCE         INTEGER,
      EVENT_ID         DECIMAL(38),
      OFFER_CODE       VARCHAR(20),
      CALL_START       TIMESTAMP
   )
LOCATION ('pxf://pivhdsne:50070/xd/job-jdbc/dynamic_basket.csv?profile=HdfsTextSimple')
FORMAT 'text' (delimiter E',' null '')
SEGMENT REJECT LIMIT 1000 ROWS
;

--
-- DYNAMIC_BASKET_EXT2 - daily load (1 insert, 3 delete, 2 update)
--
CREATE EXTERNAL TABLE DYNAMIC_BASKET_EXT2
   (
      IMPACTED_AMOUNT  DECIMAL(15,6),
      EXPIRATION_DATE  TIMESTAMP,
      REMAINING_AMOUNT DECIMAL,
      BASKET_ID        VARCHAR(8),
      INSTANCE         INTEGER,
      EVENT_ID         DECIMAL(38),
      OFFER_CODE       VARCHAR(20),
      CALL_START       TIMESTAMP
   )
LOCATION ('pxf://pivhdsne:50070/xd/job-jdbc/dynamic_basket_daily.csv?profile=HdfsTextSimple')
FORMAT 'text' (delimiter E',' null '')
SEGMENT REJECT LIMIT 1000 ROWS
;
 
--
-- DYNAMIC_BASKET
--
-- I move the key cols in the front, maybe not needed
CREATE TABLE DYNAMIC_BASKET_AO
   (
      EVENT_ID         DECIMAL(38),
      BASKET_ID        VARCHAR(8),
      INSTANCE         INTEGER,
      IMPACTED_AMOUNT  DECIMAL(15,6),
      EXPIRATION_DATE  TIMESTAMP,
      REMAINING_AMOUNT DECIMAL,
      OFFER_CODE       VARCHAR(20),
      CALL_START       TIMESTAMP,
      DELETED          BOOLEAN NOT NULL DEFAULT FALSE,
      INSERT_ID        SERIAL NOT NULL,
      NON_KEY_HASH     VARCHAR(32) NOT NULL,
      LOAD_DATE        DATE DEFAULT current_date
   )
DISTRIBUTED RANDOMLY
;

--
-- DYNAMIC_BASKET_V View of the current data
--
CREATE VIEW DYNAMIC_BASKET AS
SELECT EVENT_ID, BASKET_ID, INSTANCE, IMPACTED_AMOUNT, EXPIRATION_DATE, REMAINING_AMOUNT, OFFER_CODE, CALL_START, NON_KEY_HASH
FROM    (
        SELECT EVENT_ID, BASKET_ID, INSTANCE, IMPACTED_AMOUNT, EXPIRATION_DATE, REMAINING_AMOUNT, OFFER_CODE, CALL_START, NON_KEY_HASH, ROW_NUMBER() 
          OVER (PARTITION BY EVENT_ID,BASKET_ID,INSTANCE ORDER BY INSERT_ID DESC) AS ROWNUM, DELETED
          FROM DYNAMIC_BASKET_AO
        ) AS SUB
WHERE ROWNUM = 1 AND NOT DELETED; 

-- ########################################
-- QUERIES
-- ########################################

--
-- First Load
--
 INSERT INTO dynamic_basket_ao (
     EVENT_ID, BASKET_ID, INSTANCE,
     IMPACTED_AMOUNT, EXPIRATION_DATE, REMAINING_AMOUNT, OFFER_CODE, CALL_START, 
     NON_KEY_HASH, LOAD_DATE) 
  SELECT EVENT_ID, BASKET_ID, INSTANCE, 
         IMPACTED_AMOUNT, EXPIRATION_DATE, REMAINING_AMOUNT, OFFER_CODE, CALL_START,
         md5((IMPACTED_AMOUNT||EXPIRATION_DATE||REMAINING_AMOUNT||OFFER_CODE||CALL_START)::text), current_date 
  FROM dynamic_basket_ext;

--
-- Left join with daily load
--
select a.event_id, a.basket_id, a.instance, non_key_hash, md5 , (non_key_hash not similar to md5) as changed, (md5 is NULL) as qdeleted 
from dynamic_basket_ao a left join (
  select event_id, basket_id, instance, md5((IMPACTED_AMOUNT||EXPIRATION_DATE||REMAINING_AMOUNT||OFFER_CODE||CALL_START)::text) as md5 
  from dynamic_basket_ext2) b 
on a.event_id = b.event_id and a.basket_id = b.basket_id and a.instance = b.instance;

--
-- full join
--
select coalesce(a.event_id,b.event_id) as event_id, 
        coalesce(a.basket_id,b.basket_id) as basket_id, 
        coalesce(a.instance,b.instance) as instance, 
       (non_key_hash not similar to md5) as changed, 
       (md5 is NULL) as deleted, 
       (non_key_hash is null) as inserted 
from dynamic_basket_ao a full outer join (
  select event_id, basket_id, instance, md5((IMPACTED_AMOUNT||EXPIRATION_DATE||REMAINING_AMOUNT||OFFER_CODE||CALL_START)::text) as md5 
  from dynamic_basket_ext2) b 
on (a.event_id = b.event_id and a.basket_id = b.basket_id and a.instance = b.instance);

--
-- delta
--
select * from (
  select coalesce(a.event_id,b.event_id) as event_id, 
  coalesce(a.basket_id,b.basket_id) as basket_id, 
  coalesce(a.instance,b.instance) as instance, 
  b.impacted_amount, b.expiration_date, b.remaining_amount, b.offer_code, b.call_start, 
  (md5 is NULL) as deleted, 
  coalesce(md5, non_key_hash) as non_key_hash,
  (non_key_hash not similar to md5) as changed, 
  (non_key_hash is null) as inserted 
  from dynamic_basket a full outer join (
    select event_id, basket_id, instance, impacted_amount, expiration_date, remaining_amount, offer_code, call_start, 
    md5((IMPACTED_AMOUNT||EXPIRATION_DATE||REMAINING_AMOUNT||OFFER_CODE||CALL_START)::text) as md5 
    from dynamic_basket_ext2) b 
  on (a.event_id = b.event_id and a.basket_id = b.basket_id and a.instance = b.instance)) as changes 
where deleted or inserted or changed;

--
-- Insert differences - using delta query above
--
insert into DYNAMIC_BASKET_AO (event_id, basket_id, instance, impacted_amount, expiration_date, remaining_amount, offer_code, call_start, deleted, non_key_hash) 
  select event_id, basket_id, instance, impacted_amount, expiration_date, remaining_amount, offer_code, call_start, deleted, non_key_hash from (
  select coalesce(a.event_id,b.event_id) as event_id, 
  coalesce(a.basket_id,b.basket_id) as basket_id, 
  coalesce(a.instance,b.instance) as instance, 
  b.impacted_amount, b.expiration_date, b.remaining_amount, b.offer_code, b.call_start, 
  (md5 is NULL) as deleted, 
  coalesce(md5, non_key_hash) as non_key_hash,
  (non_key_hash not similar to md5) as changed, 
  (non_key_hash is null) as inserted 
  from dynamic_basket a full outer join (
    select event_id, basket_id, instance, impacted_amount, expiration_date, remaining_amount, offer_code, call_start, 
    md5((IMPACTED_AMOUNT||EXPIRATION_DATE||REMAINING_AMOUNT||OFFER_CODE||CALL_START)::text) as md5 
    from dynamic_basket_ext2) b 
  on (a.event_id = b.event_id and a.basket_id = b.basket_id and a.instance = b.instance)) as changes 
where deleted or inserted or changed;


