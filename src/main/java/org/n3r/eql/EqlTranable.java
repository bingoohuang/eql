package org.n3r.eql;

public interface EqlTranable {
    void start();

    void commit();

    void rollback();

    void close();
}
