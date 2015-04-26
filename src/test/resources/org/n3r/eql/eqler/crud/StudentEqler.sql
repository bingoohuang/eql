-- [prepareData]
drop table if exists eql_student;
create table eql_student(student_id int, name varchar(10), age int);

-- [addStudent]
insert into eql_student
values(##, ##, ##)

-- [queryStudentName]
select name from eql_student where student_id = ##

-- [queryStudent]
select student_id, name, age from eql_student where student_id = ##