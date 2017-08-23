package org.n3r.eql.matrix;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor @NoArgsConstructor
public class RealPartition {
    public String databaseName;
    public String tableName;

    public static RealPartition parse(String right) {
        String db = "";
        String table = "";
        if (right.startsWith(".")) {
            table = right.substring(1);
        } else {
            int dotPos = right.indexOf(".");
            if (dotPos < 0) {
                db = right;
            } else {
                db = right.substring(0, dotPos);
                table = right.substring(dotPos + 1);
            }
        }
        return new RealPartition(db, table);
    }
}
