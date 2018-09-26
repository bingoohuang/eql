package org.n3r.eql.impl;

import org.n3r.eql.base.DynamicLanguageDriver;
import org.n3r.eql.base.EqlResourceLoader;
import org.n3r.eql.base.ExpressionEvaluator;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.config.EqlConfigDecorator;
import org.n3r.eql.config.EqlConfigKeys;
import org.n3r.eql.config.EqlTranFactoryCacheLifeCycle;
import org.n3r.eql.joor.Reflect;
import org.n3r.eql.util.S;

import java.util.Map;

public class DefaultEqlConfigDecorator implements EqlConfigDecorator {
    private final EqlConfig eqlConfig;
    private EqlResourceLoader eqlResourceLoader;
    private ExpressionEvaluator expressionEvaluator;

    private EqlTranFactoryCacheLifeCycle lifeCycle;

    @Override
    public EqlResourceLoader getSqlResourceLoader() {
        return eqlResourceLoader;
    }

    public DefaultEqlConfigDecorator(EqlConfig eqlConfig) {
        this.eqlConfig = eqlConfig;
        if (eqlConfig instanceof EqlTranFactoryCacheLifeCycle)
            lifeCycle = (EqlTranFactoryCacheLifeCycle) eqlConfig;

        parseResourceLoader(eqlConfig);
        parseExpressionEvaluator(eqlConfig);
    }

    private boolean parseLazyLoad(EqlConfig eqlConfig) {
        String parseLazyStr = eqlConfig.getStr(EqlConfigKeys.SQL_PARSE_LAZY);
        return S.isBlank(parseLazyStr) || S.parseBool(parseLazyStr);
    }

    private void parseExpressionEvaluator(EqlConfig eqlConfig) {
        String evaluator = eqlConfig.getStr(EqlConfigKeys.EXPRESSION_EVALUATOR);
        expressionEvaluator = S.isBlank(evaluator) ? new OgnlEvaluator()
                : Reflect.on(evaluator).create().get();
    }

    private void parseResourceLoader(EqlConfig eqlConfig) {
        String loader = eqlConfig.getStr(EqlConfigKeys.SQL_RESOURCE_LOADER);
        eqlResourceLoader = S.isBlank(loader) ? new FileEqlResourceLoader()
                : Reflect.on(loader).create().get();
        eqlResourceLoader.setDynamicLanguageDriver(parseDynamicLanguageDriver(eqlConfig));
        eqlResourceLoader.setEqlLazyLoad(parseLazyLoad(eqlConfig));
    }

    private DynamicLanguageDriver parseDynamicLanguageDriver(EqlConfig eqlConfig) {
        String driver = eqlConfig.getStr(EqlConfigKeys.DYNAMIC_LANGUAGE_DRIVER);
        return S.isBlank(driver) ? new DefaultDynamicLanguageDriver()
                : Reflect.on(driver).create().get();
    }

    @Override
    public String getStr(String key) {
        return eqlConfig.getStr(key);
    }

    @Override
    public Map<String, String> params() {
        return eqlConfig.params();
    }

    @Override
    public ExpressionEvaluator getExpressionEvaluator() {
        return expressionEvaluator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DefaultEqlConfigDecorator that = (DefaultEqlConfigDecorator) o;

        return eqlConfig.equals(that.eqlConfig);
    }

    @Override
    public int hashCode() {
        return eqlConfig.hashCode();
    }

    @Override
    public void onLoad() {
        if (lifeCycle != null) lifeCycle.onLoad();
    }

    @Override
    public void onRemoval() {
        if (lifeCycle != null) lifeCycle.onRemoval();
    }
}
