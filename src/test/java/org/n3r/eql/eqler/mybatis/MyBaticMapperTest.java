package org.n3r.eql.eqler.mybatis;

import lombok.val;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

public class MyBaticMapperTest {
    @Test @Ignore
    public void testMyBatis() throws IOException {
        val resource = "mybatis/mybatis-conf.xml";
        val inputStream = Resources.getResourceAsStream(resource);
        val sessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        val session = sessionFactory.openSession();
        val mapper = session.getMapper(EmployeeMapper.class);


        val emp = new Employee();
        emp.setEmpid(1);
        emp.setFirstName("Manik");
        emp.setLastName("Magar");
        mapper.insertEmployee(emp);

        val employee = mapper.getEmployeeName(1);

        System.out.println(employee);

    }
}
