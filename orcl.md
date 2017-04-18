http://5aijava.iteye.com/blog/168353

//创建临时表空间

create temporary tablespace test_temp
tempfile 'E:\oracle\product\10.2.0\oradata\testserver\test_temp01.dbf'
size 32m
autoextend on
next 32m maxsize 2048m
extent management local;

//创建数据表空间
create tablespace test_data
logging
datafile 'E:\oracle\product\10.2.0\oradata\testserver\test_data01.dbf'
size 32m
autoextend on
next 32m maxsize 2048m
extent management local;

//创建用户并指定表空间
create user username identified by password
default tablespace test_data
temporary tablespace test_temp;

//给用户授予权限

grant connect,resource to username;

//以后以该用户登录，创建的任何数据库对象都属于test_temp 和test_data表空间，这就不用在每创建一个对象给其指定表空间了。

﻿[oracle@localhost ~]$ mkdir oradata
[oracle@localhost ~]$ cd oradata
[oracle@localhost oradata]$ pwd
/home/oracle/oradata
[oracle@localhost oradata]$ sqlplus /nolog

SQL*Plus: Release 11.2.0.2.0 Production on Sun Apr 13 19:05:13 2014

Copyright (c) 1982, 2010, Oracle.  All rights reserved.

SQL> conn sys/oracle as sysdba;
Connected.
SQL> create tablespace eql
  2  logging
  3  datafile '/home/oracle/oradata/eql.dbf'
  4  size 32m
  5  autoextend on
  6  next 32m maxsize 2048m
  7  extent management local;

Tablespace created.

SQL> create user orcl identified by orcl
  2  default tablespace eql;

User created.

SQL> grant connect,resource to orcl;

Grant succeeded.
