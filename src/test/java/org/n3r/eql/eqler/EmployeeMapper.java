package org.n3r.eql.eqler;

public interface EmployeeMapper {
    Employee getEmployeeName(long empId);

    void insertEmployee(Employee employee);
}
