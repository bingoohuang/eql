package org.n3r.eql.dbfieldcryptor.parser;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.n3r.eql.DbDialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static org.n3r.eql.dbfieldcryptor.parser.OracleSensitiveFieldsParser.parseOracleSql;

public class ParserCache {
    Logger logger = LoggerFactory.getLogger(ParserCache.class);

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
            logger.warn("parse sql [{}] failed {}", sql, ex.getMessage());
        }
        return null;
    }

    private SensitiveFieldsParser getSensitiveFieldsParser(String dbId, String sql) {
        if ("oracle".equals(dbId)) return parseOracleSql(sql, secureFieldsConfig);

        return null;
    }

    public SensitiveFieldsParser getParser(DbDialect dbDialect, String sql) {
        return cache.getUnchecked(new DbIdSql(dbDialect.getDatabaseId(), sql)).orNull();
    }

    static class DbIdSql {
        final String databaseId;
        final String sql;

        public DbIdSql(String databaseId, String sql) {
            this.databaseId = databaseId;
            this.sql = sql;
        }

        public String getDatabaseId() {
            return databaseId;
        }

        public String getSql() {
            return sql;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DbIdSql that = (DbIdSql) o;

            if (!databaseId.equals(that.databaseId)) return false;
            if (!sql.equals(that.sql)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = databaseId.hashCode();
            result = 31 * result + sql.hashCode();
            return result;
        }
    }

}
