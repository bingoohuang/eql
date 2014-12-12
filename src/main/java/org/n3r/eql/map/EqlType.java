package org.n3r.eql.map;

import org.n3r.eql.util.O;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum EqlType {
    SELECT, UPDATE, INSERT, MERGE, DELETE,
    DROP, CREATE, TRUNCATE, CALL, COMMENT, ALTER, BEGIN, DECLARE,
    REPLACE, UNKOWN;

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

    public boolean isProcedure() {
        return O.in(this, CALL, DECLARE, BEGIN);
    }


    private static Pattern FIRST_WORD = Pattern.compile("\\b(\\w+)\\b");

    public static EqlType parsEQLType(String rawSql) {
        Matcher matcher = FIRST_WORD.matcher(rawSql);
        matcher.find();
        String firstWord = matcher.group(1).toUpperCase();
        try {
            return EqlType.valueOf(firstWord);
        } catch (IllegalArgumentException e) {
            return UNKOWN;
        }
    }
}
