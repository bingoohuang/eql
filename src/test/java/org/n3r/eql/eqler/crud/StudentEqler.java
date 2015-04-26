package org.n3r.eql.eqler.crud;

import org.n3r.eql.eqler.annotations.EqlConfig;
import org.n3r.eql.eqler.annotations.NamedParam;
import org.n3r.eql.eqler.annotations.Sql;
import org.n3r.eql.eqler.annotations.SqlId;

import java.util.List;
import java.util.Map;

@EqlConfig("me")
public interface StudentEqler {
    void prepareData();

    int addStudent(int studentId, String name, int age);

    @Sql("insert into eql_student values(#studentId#, #name#, #age#)")
    int addStudent(Student student);

    @Sql("insert into eql_student values(#a#, #b#, #c#)")
    int addStudentAnotherWay(@NamedParam("a") int studentId, @NamedParam("b") String name, @NamedParam("c") int age);

    @Sql("select * from eql_student")
    List<Student> queryAllStudents();

    String queryStudentName(int studentId);

    @SqlId("queryStudent")
    Map<String, Object> queryStudentMap(int studentId);

    Student queryStudent(int studentId);
}
