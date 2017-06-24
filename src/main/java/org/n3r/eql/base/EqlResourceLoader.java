package org.n3r.eql.base;

import org.n3r.eql.parser.EqlBlock;

import java.util.Map;

public interface EqlResourceLoader {
    EqlBlock loadEqlBlock(String sqlClassPath, String sqlId);

    Map<String, EqlBlock> load(String classPath);

    void setDynamicLanguageDriver(DynamicLanguageDriver dynamicLanguageDriver);

    DynamicLanguageDriver getDynamicLanguageDriver();

    void setEqlLazyLoad(boolean eqlLazyLoad);
}
