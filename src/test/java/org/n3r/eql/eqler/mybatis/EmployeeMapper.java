package org.n3r.eql.eqler.mybatis;

public interface EmployeeMapper {
    Employee getEmployeeName(long empId);

    void insertEmployee(Employee employee);
}
