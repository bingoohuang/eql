package org.n3r.eql.parser;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.n3r.eql.map.EqlRun;

import java.io.StringWriter;
import java.util.Map;

import static org.n3r.eql.util.EqlUtils.mergeProperties;

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
        StringWriter writer = new StringWriter();
        try {
            Map<String, Object> executionContext = eqlRun.getExecutionContext();
            Map<String, Object> rootMap = mergeProperties(executionContext, eqlRun.getParamBean());
            ftlTemplate.process(rootMap, writer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return writer.toString();
    }

}
