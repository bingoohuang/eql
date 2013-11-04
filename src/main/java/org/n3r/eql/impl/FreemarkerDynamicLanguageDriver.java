package org.n3r.eql.impl;

import com.google.common.base.Joiner;
import freemarker.cache.StringTemplateLoader;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModelException;
import org.n3r.eql.base.DynamicLanguageDriver;
import org.n3r.eql.parser.EqlBlock;
import org.n3r.eql.parser.FreemarkerSql;import org.n3r.eql.parser.Sql;
import org.n3r.eql.parser.StaticSql;
import org.n3r.eql.util.EqlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class FreemarkerDynamicLanguageDriver implements DynamicLanguageDriver {

    @Override
    public Sql parse(EqlBlock block, List<String> oneSqlLines) {
        String template = Joiner.on('\n').join(oneSqlLines);

        if (template.indexOf("<#") < 0) return new StaticSql(template);

        Configuration ftlConfig = new Configuration();
        StringTemplateLoader stringLoader = new StringTemplateLoader();
        ftlConfig.setTemplateLoader(stringLoader);

        String uniqueSqlId = EqlUtils.uniqueSqlId(block.getSqlClassPath(), block.getSqlId());
        stringLoader.putTemplate(uniqueSqlId, template);
        Template temp;
        try {
            temp = ftlConfig.getTemplate(uniqueSqlId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new FreemarkerSql(ftlConfig, temp);
    }


}
