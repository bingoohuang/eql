-- [test1]
select 'a' from dual where '##' = 'a' and '#dpcode:context#' = 0 and '##' = 'b';

-- [test2]
select 'a' from dual where '#a#' = 'a' and '#dpcode:context#' = 0 and '#b#' = 'b';

-- [test3]
select 'a' from dual where '#2#' = 'b' and '#dpcode:context#' = 0 and '#1#' = 'a';

-- [test4]
insert into t_dpcode(name, dpcode, remark) values('##', '#dpcode:context#', '##');

-- [select]
select name, dpcode, remark from t_dpcode where dpcode = '#dpcode:context#';

