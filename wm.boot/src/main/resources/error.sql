--
-- cmdtime, relname, filename, linenum, bytenum, errmsg, rawdata, rawbytes
--

-- total errors
select count(*) from err_wm;

-- total errors by table
select relname, count(relname) from err_wm;

-- total errors by file/table
select relname, count(relname), filename from err_wm group by filename,relname;

-- file
select to_char(cmdtime, 'DD Mon YYYY HH24:MI:SS') as cmdtime,count(*),filename from err_wm group by cmdtime,filename ;

-- files with errors
select distinct filename from err_wm ;

-- External tables with issues
select distinct relname from err_wm;