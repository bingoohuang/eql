package org.n3r.eql.impl;

import org.junit.Test;
import org.n3r.eql.Eql;

import java.sql.Connection;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class GetConnectionTest {
    @Test
    public void getJdbcConnection() throws SQLException {
        Connection connection = new Eql("me").getConnection();
        assertThat(connection, is(notNullValue()));
        connection.close();

        connection = new Eql("me").getConnection();
        assertThat(connection, is(notNullValue()));
        connection.close();
    }

    @Test
    public void getJndiConnection() throws SQLException {
        Connection connection = new Eql().getConnection();
        assertThat(connection, is(notNullValue()));
        connection.close();

        connection = new Eql().getConnection();
        assertThat(connection, is(notNullValue()));
        connection.close();
    }
}
