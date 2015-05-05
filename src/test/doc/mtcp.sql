create database dba;
create user 'dba'@'%' identified by 'dba';
grant all privileges on dba.* to dba@'%';

create database dbb;
create user 'dbb'@'%' identified by 'dbb';
grant all privileges on dbb.* to dbb@'%';

create database dbc;
create user 'dbc'@'%' identified by 'dbc';
grant all privileges on dbc.* to dbc@'%';

create database diamond;
create user 'diamond'@'%' identified by 'diamond';
grant all privileges on diamond.* to diamond@'%';

show create table mtcp;
create table mtcp ( cnt int(11) );
insert into dba.mtcp values(0);
insert into dbb.mtcp values(1);
insert into dbc.mtcp values(2);
insert into diamond.mtcp values(3);

drop table if exists mtcp_props;

create table mtcp_props(
  tenant_id varchar(32),
  host varchar(32),
  port varchar(32),
  dbtype varchar(32),
  dbname varchar(100),
  username varchar(32),
  password varchar(32),
  primary key(tenant_id));

insert into mtcp_props(tenant_id, host, port, dbtype, dbname, username, password)
values('mtcp-dba', "localhost", "3306", 'mysql', 'dba', 'dba', 'dba');

insert into mtcp_props(tenant_id, host, port, dbtype, dbname, username, password)
values('mtcp-dbb', "localhost", "3306", 'mysql', 'dbb', 'dbb', 'dbb');

insert into mtcp_props(tenant_id, host, port, dbtype, dbname, username, password)
values('mtcp-dbc', "localhost", "3306", 'mysql', 'dbc', 'dbc', 'dbc');

insert into mtcp_props(tenant_id, host, port, dbtype, dbname, username, password)
values('mtcp-diamond', "localhost", "3306", 'mysql', 'diamond', 'diamond', 'diamond');
