package org.n3r.eql.parser;

import com.google.common.base.Splitter;
import org.n3r.eql.map.EqlRun;
import org.n3r.eql.util.S;

import java.util.Collections;
import java.util.List;

public class TrimPart implements EqlPart {
    private String prefix, suffix;
    private List<String> prefixOverrides, suffixOverrides;
    private MultiPart multiPart;

    public TrimPart(String prefix, String suffix, String prefixOverrides, String suffixOverrides, MultiPart multiPart) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.prefixOverrides = split(prefixOverrides);
        this.suffixOverrides = split(suffixOverrides);
        this.multiPart = multiPart;
    }

    private List<String> split(String overrides) {
        if (S.isBlank(overrides)) return Collections.emptyList();

        return Splitter.on('|').trimResults().omitEmptyStrings().splitToList(overrides.toLowerCase());
    }

    @Override
    public String evalSql(EqlRun eqlRun) {
        StringBuilder sql = new StringBuilder();

        partSql = multiPart.evalSql(eqlRun);
        if (!S.isBlank(partSql)) {
            override();

            if (S.isNotEmpty(prefix)) {
                sql.append(prefix).append(' ');
                partSql = S.trimLeft(partSql);
            }

            if (S.isNotEmpty(suffix)) {
                sql.append(S.trimRight(partSql));
                sql.append(' ').append(suffix);
            } else {
                sql.append(partSql);
            }

        } else {
            sql.append(partSql);
        }


        return sql.toString();
    }


    private String partSql, lowerSql;

    private String override() {
        lowerSql = partSql.toLowerCase();

        for (String prefixOverride : prefixOverrides) {
            if (lowerSql.startsWith(prefixOverride)) {
                overridePrefix(prefixOverride);
            }
        }

        for (String suffixOverride : suffixOverrides) {
            if (endsWith(suffixOverride)) {
                overrideSuffix(suffixOverride);
            }
        }

        return partSql;
    }

    private boolean endsWith(String suffix) {
        return lowerSql.trim().endsWith(suffix);
    }

    private void overrideSuffix(String suffix) {
        String right = S.trimRight(lowerSql);
        int diff = lowerSql.length() - right.length();
        String diffStr = diff <= 0 ? "" : lowerSql.substring(right.length());

        int suffixLen = suffix.length();
        int strLen = right.length();
        if (strLen > suffixLen) {
            lowerSql = S.trimLeft(lowerSql.substring(0, strLen - suffixLen)) + diffStr;
            partSql = S.trimLeft(partSql.substring(0, strLen - suffixLen)) + diffStr;
        } else {
            lowerSql = "";
            partSql = "";
        }
    }

    private void overridePrefix(String prefix) {
        int startIndex = prefix.length();
        if (startIndex < lowerSql.length()) {
            lowerSql = lowerSql.substring(startIndex);
            partSql = partSql.substring(startIndex);
        } else {
            lowerSql = "";
            partSql = "";
        }
    }

    public MultiPart getParts() {
        return multiPart;
    }
}
