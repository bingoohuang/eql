package org.n3r.eql.ex;

public class EqlConfigException extends EqlException {
    private static final long serialVersionUID = -654333785604100786L;

    public EqlConfigException(String msg) {
        super(msg);
    }

    public EqlConfigException(String msg, Throwable e) {
        super(msg, e);
    }

    public EqlConfigException(Throwable e) {
        super(e);
    }

}
