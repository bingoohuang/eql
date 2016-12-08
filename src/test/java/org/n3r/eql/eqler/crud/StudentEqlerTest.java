package org.n3r.eql.eqler.crud;

import org.junit.Test;
import org.n3r.eql.eqler.EqlerFactory;

import java.util.List;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;


public class StudentEqlerTest {
    @Test
    public void test() {
        StudentEqler eqler = EqlerFactory.getEqler(StudentEqler.class);
        eqler.prepareData();

        eqler.addStudent(1, "bingoo", 123);
        eqler.addStudent(new Student(2, "huang", 124));
        eqler.addStudentAnotherWay(3, "dingoo", 125);

        List<Student> students = eqler.queryAllStudents();
        assertThat(students.toString()).isEqualTo(
                "[Student(studentId=1, name=bingoo, age=123), " +
                        "Student(studentId=2, name=huang, age=124), " +
                        "Student(studentId=3, name=dingoo, age=125)]"
        );

        Student student1 = eqler.queryStudent(1);
        assertThat(student1.toString()).isEqualTo("Student(studentId=1, name=bingoo, age=123)");

        Map<String, Object> student2 = eqler.queryStudentMap(2);
        assertThat(student2.toString()).isEqualTo("{name=huang, student_id=2, age=124}");

        String studentName = eqler.queryStudentName(1);
        assertThat(studentName).isEqualTo("bingoo");
    }

}
