package org.n3r.eql.dbfieldcryptor.parser;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.n3r.eql.DbDialect;

import java.util.Set;


@Slf4j
public class ParserCache {
    final Set<String> secureFieldsConfig;
    final LoadingCache<DbIdSql, Optional<SensitiveFieldsParser>> cache;

    public ParserCache(Set<String> secureFieldsConfig) {
        this.secureFieldsConfig = secureFieldsConfig;

        cache = CacheBuilder.newBuilder().build(
                new CacheLoader<DbIdSql, Optional<SensitiveFieldsParser>>() {
                    @Override
                    public Optional<SensitiveFieldsParser> load(DbIdSql dbIdSql) {
                        return Optional.fromNullable(getParser(dbIdSql));
                    }
                });
    }

    private SensitiveFieldsParser getParser(DbIdSql dbIdSql) {
        String databaseId = dbIdSql.getDatabaseId();
        String sql = dbIdSql.getSql();

        SensitiveFieldsParser parser;
        try {
            parser = getSensitiveFieldsParser(databaseId, sql);

            if (parser == null) return null;
            if (parser.haveNonSecureFields()) return null;
            return parser;

        } catch (Exception ex) {
            log.warn("parse sql [{}] failed {}", sql, ex.getMessage());
        }
        return null;
    }

    private SensitiveFieldsParser getSensitiveFieldsParser(String dbId, String sql) {
        if ("oracle".equals(dbId))
            return OracleSensitiveFieldsParser.parseSql(sql, secureFieldsConfig);
        if ("mysql".equals(dbId))
            return MySqlSensitiveFieldsParser.parseSql(sql, secureFieldsConfig);

        return null;
    }

    public SensitiveFieldsParser getParser(DbDialect dbDialect, String sql) {
        return cache.getUnchecked(new DbIdSql(dbDialect.getDatabaseId(), sql)).orNull();
    }

    @Getter @AllArgsConstructor @EqualsAndHashCode
    static class DbIdSql {
        final String databaseId;
        final String sql;
    }

}
