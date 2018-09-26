-- [test1]
select 'a' from dual where '##' = 'a' and '#dpcode:contextOnly#' = 0 and '##' = 'b';

-- [testEx1]
select 'a' from dual where '#1#' = 'a' and '#dpcode:contextOnly#' = 0 and '##' = 'b';

-- [testEx2]
select 'a' from dual where '#a#' = 'a' and '#dpcode:contextOnly#' = 0 and '#1#' = 'b';

-- [testEx3]
select 'a' from dual where '#a#' = 'a' and '#dpcode:contextOnly#' = 0 and '#1#' = 'b';

-- [test2]
select 'a' from dual where '#a#' = 'a' and '#dpcode:contextOnly#' = 0 and '#b#' = 'b';

-- [test20]
select 'a' from dual where '#a#' = 'a' and '#dpcode:context#' = 0 and '#b#' = 'b';

-- [test3]
select 'a' from dual where '#2#' = 'b' and '#dpcode:contextOnly#' = 0 and '#1#' = 'a';

-- [test4]
insert into t_dpcode(name, dpcode, remark) values('##', '#dpcode:contextOnly#', '##');

-- [test41]
insert into t_dpcode(name, dpcode, remark) values('#name:context#', '#dpcode:context#', '#remark:context#');

-- [test42]
insert into t_dpcode(name, dpcode, remark) values('#?#', '#dpcode:context#', '#?#');

-- [select]
select name, dpcode, remark from t_dpcode where dpcode = '#dpcode:context#';
