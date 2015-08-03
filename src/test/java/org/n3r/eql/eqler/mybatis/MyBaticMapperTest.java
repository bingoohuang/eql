package org.n3r.eql.eqler.mybatis;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

public class MyBaticMapperTest {
    @Ignore
    @Test
    public void testMyBatis() throws IOException {
        String resource = "mybatis/mybatis-conf.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);
        SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        SqlSession session = sessionFactory.openSession();
        EmployeeMapper mapper = session.getMapper(EmployeeMapper.class);


        Employee emp = new Employee();
        emp.setEmpid(1);
        emp.setFirstName("Manik");
        emp.setLastName("Magar");
        mapper.insertEmployee(emp);

        Employee employee = mapper.getEmployeeName(1);

        System.out.println(employee);

    }
}
