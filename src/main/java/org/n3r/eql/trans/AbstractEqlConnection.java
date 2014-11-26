package org.n3r.eql.trans;

import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.map.EqlRun;

public abstract class AbstractEqlConnection implements EqlConnection{
    @Override
    public String getDbName(EqlConfig eqlConfig, EqlRun eqlRun) {
        return "auto";
    }
}
