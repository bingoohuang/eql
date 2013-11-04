package org.n3r.eql.impl;

import org.n3r.eql.base.DynamicLanguageDriver;
import org.n3r.eql.base.EqlResourceLoader;
import org.n3r.eql.base.ExpressionEvaluator;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.config.EqlConfigDecorator;
import org.n3r.eql.joor.Reflect;
import org.n3r.eql.util.EqlUtils;

public class DefaultEqlConfigDecorator implements EqlConfigDecorator {
    private final EqlConfig eqlConfig;
    private EqlResourceLoader eqlResourceLoader;
    private ExpressionEvaluator expressionEvaluator;

    @Override
    public EqlResourceLoader getSqlResourceLoader() {
        return eqlResourceLoader;
    }

    public DefaultEqlConfigDecorator(EqlConfig eqlConfig) {
        this.eqlConfig = eqlConfig;

        parseResourceLoader(eqlConfig);
        parseExpressionEvaluator(eqlConfig);
    }

    private void parseExpressionEvaluator(EqlConfig eqlConfig) {
        String evaluator = eqlConfig.getStr("expression.evaluator");
        expressionEvaluator = EqlUtils.isBlank(evaluator) ? new OgnlEvaluator()
                : Reflect.on(evaluator).create().<ExpressionEvaluator>get();
    }

    private void parseResourceLoader(EqlConfig eqlConfig) {
        String loader = eqlConfig.getStr("sql.resource.loader");
        eqlResourceLoader = EqlUtils.isBlank(loader) ? new FileEqlResourceLoader()
                : Reflect.on(loader).create().<EqlResourceLoader>get();
        eqlResourceLoader.setDynamicLanguageDriver(parseDynamicLanguageDriver(eqlConfig));
    }

    private DynamicLanguageDriver parseDynamicLanguageDriver(EqlConfig eqlConfig) {
        String driver = eqlConfig.getStr("dynamic.language.driver");
        return EqlUtils.isBlank(driver) ? new DefaultDynamicLanguageDriver()
                : Reflect.on(driver).create().<DynamicLanguageDriver>get();
    }

    @Override
    public String getStr(String key) {
        return eqlConfig.getStr(key);
    }

    @Override
    public ExpressionEvaluator getExpressionEvaluator() {
        return expressionEvaluator;
    }
}
