package org.n3r.eql.map;

public enum EqlType {
    SELECT, UPDATE, INSERT, MERGE, DELETE,
    DROP, CREATE, TRUNCATE, CALL, COMMENT, ALTER, BEGIN, DECLARE,
    REPLACE;

    public boolean isUpdateStmt() {
        switch (this) {
            case UPDATE:
            case MERGE:
            case DELETE:
            case INSERT:
            case REPLACE:
                return true;
            default:
                break;
        }
        return false;
    }


    public boolean isDdl() {
        switch (this) {
            case CREATE:
            case DROP:
            case TRUNCATE:
            case ALTER:
            case COMMENT:
                return true;
            default:
                break;
        }
        return false;
    }
}
