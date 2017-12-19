package org.n3r.eql.dbfieldcryptor.parser;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;
import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import com.alibaba.druid.sql.visitor.SQLASTVisitorAdapter;
import lombok.experimental.var;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Set;

@Slf4j
public class MySqlSensitiveFieldsParser extends SensitiveFieldsParser {
    public MySqlSensitiveFieldsParser(Set<String> secureFields, String sql) {
        super(secureFields, sql);
    }


    @Override protected SQLASTVisitorAdapter getAdapter() {
        return adapter;
    }

    @Override protected SQLASTVisitor createSQLVariantVisitor(final SQLASTVisitorAdapter visitor) {
        return new MySqlASTVisitorAdapter() {
            @Override
            public boolean visit(SQLVariantRefExpr x) {
                return visitor.visit(x);
            }
        };
    }

    private SQLASTVisitorAdapter adapter = new MySqlASTVisitorAdapter() {
        @Override
        public boolean visit(SQLVariantRefExpr x) {
            return adapter_impl.visit(x);
        }

        @Override
        public boolean visit(SQLBinaryOpExpr x) {
            return adapter_impl.visit(x);
        }
    };

    private static SensitiveFieldsParser tryParseHint(String sql, Set<String> secureFields) {
        val matcher = encryptHint.matcher(sql);
        if (matcher.find() && matcher.start() == 0) {
            val convertedSql = sql.substring(matcher.end());
            val hint = matcher.group(1);
            val fieldsParser = new MySqlSensitiveFieldsParser(secureFields, convertedSql);
            fieldsParser.parseHint(hint);
            return fieldsParser;
        }

        return null;
    }

    private static SQLStatement parseSql(String sql) {
        val parser = new MySqlStatementParser(sql);
        try {
            return parser.parseStatement();
        } catch (ParserException exception) {
            exception.printStackTrace();
            throw new RuntimeException(sql + " is invalid, detail " + exception.getMessage());
        }
    }

    public static SensitiveFieldsParser parseSql(String sql, Set<String> secureFields) {
        var fieldsParser = tryParseHint(sql, secureFields);
        if (fieldsParser == null) {
            val sqlStatement = parseSql(sql);
            val sensitiveFieldsParser = new MySqlSensitiveFieldsParser(secureFields, sql);
            fieldsParser = SensitiveFieldsParser.parseStatement(sensitiveFieldsParser, sqlStatement);
        }

        if (fieldsParser == null) return null;
        if (fieldsParser.haveNonSecureFields()) return null;
        return fieldsParser;
    }

}
