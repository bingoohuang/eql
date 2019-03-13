package org.n3r.eql.codedesc;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.val;
import org.n3r.eql.util.KeyValue;
import org.n3r.eql.util.S;

public class CodeDescSettings {
    static Cache<String, CodeDescMapper> codeDescCache = CacheBuilder.newBuilder().build();

    public static void processSetting(KeyValue codeDescSetting) {
        codeDescCache.put(codeDescSetting.getKey(), parseCodeDescMapper(codeDescSetting.getValue()));
    }

    private static CodeDescMapper parseCodeDescMapper(String mapperExpr) {
        val bracePos = mapperExpr.indexOf('(');
        val mapperAlias = bracePos < 0 ? mapperExpr : mapperExpr.substring(0, bracePos);
        val valueStr = bracePos < 0 ? "" : S.substrInQuotes(mapperExpr, '(', bracePos);

        if ("decode".equalsIgnoreCase(mapperAlias)) return new DecodeCodeDescMapper(valueStr);
        if ("mapping".equalsIgnoreCase(mapperAlias)) return new MappingCodeDescMapper(valueStr);

        return null;
    }

    public static String map(CodeDesc codeDesc, String code) {
        val codeDescMapper = codeDescCache.getIfPresent(codeDesc.getDescLabel());
        if (codeDescMapper != null) return codeDescMapper.map(code);

        return null;
    }
}
