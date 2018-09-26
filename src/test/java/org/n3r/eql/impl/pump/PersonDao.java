package org.n3r.eql.impl.pump;

import org.n3r.eql.Eqll;
import org.n3r.eql.eqler.annotations.EqlerConfig;
import org.n3r.eql.eqler.annotations.Sql;

@EqlerConfig(eql = Eqll.class)
public interface PersonDao {
    @Sql("truncate table person")
    void truncatePerson();

    @Sql("insert into person(id, name, sex, addr) values(#id#, #name#, #sex#, #addr#)")
    void addPerson(Person person);
}
