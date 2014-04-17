package org.n3r.eql.app;


import org.junit.Ignore;
import org.junit.Test;
import org.n3r.eql.Eql;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class AdminService {
    @Test
    @Ignore
    public void test() {
        new Eql().id("setup").execute();
        Admin lvyong = findAdmin("lvyong");
        assertThat(lvyong, is(notNullValue()));
    }

    public Admin findAdmin(String username) {
        Admin admin = new Eql("jndi")
                .selectFirst("findAdmin")
                .params(username)
                .returnType(Admin.class)
                .execute();

        return admin;
    }

}
