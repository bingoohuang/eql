package org.n3r.eql.parser;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateModelException;
import org.n3r.eql.map.EqlRun;

import java.io.StringWriter;
import java.util.Map;

public class FreemarkerSql implements Sql {
    private final Template ftlTemplate;
    private final Configuration ftlConfig;

    public FreemarkerSql(Configuration ftlConfig, Template ftlTemplate) {
        this.ftlConfig = ftlConfig;
        this.ftlTemplate = ftlTemplate;
    }

    @Override
    public String evalSql(EqlRun eqlRun) {
        return process(eqlRun);
    }

    public String process(EqlRun eqlRun) {
        Map<String, Object> executionContext = eqlRun.getExecutionContext();
        setSharedVairable(executionContext);

        String result = evaluate(eqlRun);

        unsetSharedVairable(executionContext);

        return result;
    }

    private String evaluate(EqlRun eqlRun) {
        StringWriter writer = new StringWriter();
        try {
            ftlTemplate.process(eqlRun.getParamBean(), writer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return writer.toString();
    }

    private void setSharedVairable(Map<String, Object> executionContext) {
        try {
            for (Map.Entry<String, Object> entry : executionContext.entrySet()) {
                ftlConfig.setSharedVariable(entry.getKey(), entry.getValue());
            }
        } catch (TemplateModelException e) {
            throw new RuntimeException(e);
        }
    }

    private void unsetSharedVairable(Map<String, Object> executionContext) {
        try {
            for (Map.Entry<String, Object> entry : executionContext.entrySet()) {
                ftlConfig.setSharedVariable(entry.getKey(), (Object) null);
            }
        } catch (TemplateModelException e) {
            throw new RuntimeException(e);
        }
    }
}
