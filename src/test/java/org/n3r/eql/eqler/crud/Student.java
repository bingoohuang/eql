package org.n3r.eql.eqler.crud;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor @NoArgsConstructor @Data
public class Student {
    private int studentId;
    private String name;
    private int age;
}
