package org.n3r.eql.base;

import org.n3r.eql.map.EqlRun;

public interface ExpressionEvaluator {
    Object eval(String expr, EqlRun eqlRun);

    Object evalDynamic(String expr, EqlRun eqlRun);

    boolean evalBool(String expr, EqlRun eqlRun);
}
