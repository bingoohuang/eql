package org.n3r.eql.impl;

import com.google.common.base.Joiner;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.val;
import org.n3r.eql.base.DynamicLanguageDriver;
import org.n3r.eql.parser.EqlBlock;
import org.n3r.eql.parser.FreemarkerSql;
import org.n3r.eql.parser.Sql;
import org.n3r.eql.parser.StaticSql;

import java.util.List;

public class FreemarkerDynamicLanguageDriver implements DynamicLanguageDriver {
    @Override @SneakyThrows
    public Sql parse(EqlBlock block, List<String> oneSqlLines) {
        val template = Joiner.on('\n').join(oneSqlLines);
        if (template.indexOf("<#") < 0) {
            return new StaticSql(template);
        }

        val ftlConfig = new Configuration();
        val stringLoader = new StringTemplateLoader();
        ftlConfig.setTemplateLoader(stringLoader);

        val uniqueEqlIdStr = block.getUniqueSqlIdStr();
        stringLoader.putTemplate(uniqueEqlIdStr, template);
        val temp = ftlConfig.getTemplate(uniqueEqlIdStr);

        return new FreemarkerSql(ftlConfig, temp);
    }

}
