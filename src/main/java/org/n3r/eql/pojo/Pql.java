package org.n3r.eql.pojo;


import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import org.apache.commons.io.Charsets;
import org.n3r.eql.Eql;
import org.n3r.eql.EqlPage;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.impl.DefaultDynamicLanguageDriver;
import org.n3r.eql.parser.EqlBlock;
import org.n3r.eql.parser.EqlBlockParser;
import org.n3r.eql.pojo.impl.PojoParser;
import org.n3r.eql.util.P;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * For POJO operations without direct sql required.
 */
public class Pql extends Eql {
    private Object pojo;
    private String sql;

    public Pql() {
        super();
    }

    public Pql(EqlConfig eqlConfig) {
        super(eqlConfig);
    }

    public Pql(String connectionName) {
        super(connectionName);
    }

    public <T> void create(T pojo) {
        this.pojo = pojo;
        this.sql = PojoParser.parseCreateSql(pojo.getClass());

        super.id("create").params(pojo).execute();
    }

    public <T> int update(T pojo, String... includeProperties) {
        this.pojo = pojo;
        int includePropertiesSize = includeProperties.length;

        if (includePropertiesSize == 0) {
            this.sql = PojoParser.parseUpdateSql(pojo.getClass());
            String sqlid = Hashing.murmur3_32().hashString(sql, Charsets.UTF_8).toString();
            return super.id(sqlid).params(pojo).execute();
        }

        this.sql = PojoParser.parseUpdateSql2(pojo.getClass());
        String input = sql + Arrays.toString(includeProperties);
        String sqlid = Hashing.murmur3_32().hashString(input, Charsets.UTF_8).toString();

        Map<String, Object> params = Maps.newHashMap();
        for (String includeProperty : includeProperties) {
            params.put(PojoParser.PREFIX_FLAG + includeProperty, "TAG");
        }

        Map<String, Object> mergeProperties = P.mergeProperties(params, pojo);

        return super.id(sqlid).params(mergeProperties).execute();
    }

    public <T> T read(Object pojo) {
        this.pojo = pojo;
        this.sql = PojoParser.parseReadSql(pojo.getClass());
        return super.id("read").params(pojo).returnType(pojo.getClass()).execute();
    }

    public <T> int delete(T pojo) {
        this.pojo = pojo;
        this.sql = PojoParser.parseDeleteSql(pojo.getClass());
        return super.id("delete").params(pojo).execute();
    }

    @Override
    public Pql limit(EqlPage page) {
        return (Pql) super.limit(page);
    }

    @Override
    public Pql limit(int maxRows) {
        return (Pql) super.limit(maxRows);
    }

    protected void initSqlId(String sqlId, int level) {
        String sqlClassPath = pojo.getClass().getName();
        eqlBlock = new EqlBlock(sqlClassPath, sqlId, "", 0);

        EqlBlockParser blockParser = new EqlBlockParser(new DefaultDynamicLanguageDriver(), false);
        List<String> sqlLines = Splitter.on("\r\n").splitToList(sql);
        blockParser.parse(eqlBlock, sqlLines);

        rsRetriever.setEqlBlock(eqlBlock);
    }
}
