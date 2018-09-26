package org.n3r.eql.impl.pump;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Person {
    String id;
    String name;
    String addr;
    int sex;

    public static Person create(String id, String name, String addr, int sex) {
        return new Person(id, name, addr, sex);
    }
}
