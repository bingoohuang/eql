package org.n3r.eql;

import org.n3r.eql.config.EqlConfigable;

public class Eqll extends Eql {
    private static ThreadLocal<Object> connectionNameOrConfigable = new ThreadLocal<Object>(){
        @Override
        protected Object initialValue() {
            return  Eql.DEFAULT_CONN_NAME;
        }
    };

    public static void choose(String connectionName) {
        connectionNameOrConfigable.set(connectionName);
    }
    public static void choose(EqlConfigable eqlConfigable) {
        connectionNameOrConfigable.set(eqlConfigable);
    }

    public Eqll() {
        super(connectionNameOrConfigable.get());
    }
}
