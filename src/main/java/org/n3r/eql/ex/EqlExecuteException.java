package org.n3r.eql.ex;

public class EqlExecuteException extends EqlException {
    private static final long serialVersionUID = -4580346119774034899L;

    public EqlExecuteException(String msg) {
        super(msg);
    }

    public EqlExecuteException(Throwable e) {
        super(e);
    }

    public EqlExecuteException(String msg, Throwable e) {
        super(msg, e);
    }

}
