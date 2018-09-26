package org.n3r.eql.parser;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.n3r.eql.map.EqlRun;

@AllArgsConstructor
public class LiteralPart implements EqlPart {
    @Getter private String sql;

    public void appendComment(String comment) {
        if (comment.startsWith("--")) {
            sql += "\n" + comment + "\n";
        } else {
            sql += comment;
        }
    }

    public void appendSql(String line) {
        if (sql.length() > 0)
            sql += " " + line;
        else
            sql = line;
    }

    @Override
    public String evalSql(EqlRun eqlRun) {
        return sql;
    }
}
