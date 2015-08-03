package org.n3r.eql.ex;

public class EqlException extends RuntimeException {
    private static final long serialVersionUID = -8224740965898901169L;

    public EqlException(String msg) {
        super(msg);
    }

    public EqlException(Throwable e) {
        super(e);
    }

    public EqlException(String msg, Throwable e) {
        super(msg, e);
    }

}
