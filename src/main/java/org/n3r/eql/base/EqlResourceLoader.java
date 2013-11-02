package org.n3r.eql.base;

import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.parser.EqlBlock;
import org.n3r.eql.parser.EqlParser;

import java.util.Map;

public interface EqlResourceLoader {
    void initialize(EqlConfig eqlConfig);

    EqlBlock loadEqlBlock(String sqlClassPath, String sqlId);

    Map<String,EqlBlock> load(String classPath);
}
