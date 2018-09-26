package org.n3r.eql.impl;

import org.n3r.eql.ex.EqlExecuteException;
import org.n3r.eql.map.EqlRun;
import org.n3r.eql.parser.EqlBlock;

import java.util.List;

public class IterateOptions {
    public static void checkIterateOption(
            EqlBlock eqlBlock, List<EqlRun> eqlRuns, Object[] params) {
        if (!eqlBlock.isIterateOption()) return;

        if (eqlRuns.size() != 1)
            throw new EqlExecuteException
                    ("iterate mode only allow enabled when only one sql in a block");

        EqlRun eqlRun = eqlRuns.get(0);
        if (!eqlRun.getSqlType().isUpdateStmt())
            throw new EqlExecuteException(
                    "iterate mode only allow enabled when sql type is update");

        if (params == null || params.length == 0)
            throw new EqlExecuteException(
                    "batch mode only allow enabled when single parameter in collection type");

        if (params[0] instanceof Iterable && params.length == 1) return;
        if (params[0] != null && params[0].getClass().isArray()
                && params.length == 2 && params[1] == null) return;

        throw new EqlExecuteException(
                "batch mode only allow enabled when single parameter of iteratable or array type");
    }

}
