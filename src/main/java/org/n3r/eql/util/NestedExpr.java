package org.n3r.eql.util;

import lombok.Getter;

import java.util.regex.Pattern;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/2/10.
 */
@Getter
public class NestedExpr {
    static Pattern normalPattern = Pattern.compile("[_\\w][_\\w\\d]*");
    private boolean nested;
    private String subExpr;
    private String parentExpr;

    public NestedExpr(String expr) {
        int dotPos = expr.indexOf('.');
        if (dotPos > 0) {
            this.parentExpr = expr.substring(0, dotPos);
            this.subExpr = expr.substring(dotPos + 1);
            this.nested = (normalPattern.matcher(parentExpr).matches() &&
                    normalPattern.matcher(subExpr).matches());
        } else {
            this.nested = false;
        }
    }
}
