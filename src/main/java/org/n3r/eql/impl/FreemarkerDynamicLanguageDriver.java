package org.n3r.eql.impl;

import com.google.common.base.Joiner;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.n3r.eql.base.DynamicLanguageDriver;
import org.n3r.eql.parser.EqlBlock;
import org.n3r.eql.parser.FreemarkerSql;
import org.n3r.eql.parser.Sql;
import org.n3r.eql.parser.StaticSql;

import java.io.IOException;
import java.util.List;

public class FreemarkerDynamicLanguageDriver implements DynamicLanguageDriver {

    @Override
    public Sql parse(EqlBlock block, List<String> onEQLLines) {
        String template = Joiner.on('\n').join(onEQLLines);

        if (template.indexOf("<#") < 0) return new StaticSql(template);

        Configuration ftlConfig = new Configuration();
        StringTemplateLoader stringLoader = new StringTemplateLoader();
        ftlConfig.setTemplateLoader(stringLoader);

        String uniquEQLIdStr = block.getUniquEQLIdStr();
        stringLoader.putTemplate(uniquEQLIdStr, template);
        Template temp;
        try {
            temp = ftlConfig.getTemplate(uniquEQLIdStr);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new FreemarkerSql(ftlConfig, temp);
    }

}
