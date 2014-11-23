package org.n3r.eql.codedesc;

import com.google.common.base.Splitter;
import org.n3r.eql.base.EqlResourceLoader;
import org.n3r.eql.config.EqlConfigDecorator;
import org.n3r.eql.ex.EqlConfigException;
import org.n3r.eql.ex.EqlExecuteException;
import org.n3r.eql.map.EqlRun;
import org.n3r.eql.parser.EqlBlock;
import org.n3r.eql.spec.Spec;
import org.n3r.eql.spec.SpecParser;
import org.n3r.eql.util.Rs;
import org.n3r.eql.util.S;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CodeDescs {
    public static ResultSet codeDescWrap(EqlRun currEqlRun, EqlBlock eqlBlock,
                                         EqlConfigDecorator eqlConfig,
                                         String sqlClassPath, ResultSet rs) {
        List<CodeDesc> descs = eqlBlock.getCodeDescs();
        if (descs == null) return rs;

        if (!existsReturnDescColumns(descs, rs)) return rs;

        return new CodeDescResultSetHandler(currEqlRun, eqlConfig, sqlClassPath, rs, descs).createProxy();
    }

    private static boolean existsReturnDescColumns(List<CodeDesc> descs, ResultSet rs) {
        try {
            ResultSetMetaData metaData = rs.getMetaData();
            for (int i = 0, ii = metaData.getColumnCount(); i < ii; ++i) {
                String columnName = Rs.lookupColumnName(metaData, i + 1);

                for (CodeDesc codeDesc : descs) {
                    if (codeDesc.getColumnName().equals(columnName)) return true;
                }
            }

            return false;
        } catch (SQLException e) {
            throw new EqlExecuteException(e);
        }
    }

    public static List<CodeDesc> parseOption(EqlBlock eqlBlock, Map<String, String> options) {
        String desc = options.get("desc");
        if (S.isBlank(desc)) return null;

        List<CodeDesc> codeDescs = new ArrayList<CodeDesc>();
        Splitter descParts = Splitter.on(',').omitEmptyStrings().trimResults();
        for (String descPart : descParts.split(desc)) {
            CodeDesc codeDesc = parseCodeDesc(eqlBlock, descPart);
            codeDescs.add(codeDesc);
        }

        return codeDescs.size() == 0 ? null : codeDescs;
    }

    private static CodeDesc parseCodeDesc(EqlBlock eqlBlock, String descPart) {
        int atPos = descPart.indexOf('@');
        check(eqlBlock, atPos > 0);
        String columnName = descPart.substring(0, atPos);
        String reference = descPart.substring(atPos);
        Spec spec = SpecParser.parseSpec(reference);

        return new CodeDesc(columnName, spec);
    }

    private static void check(EqlBlock eqlBlock, boolean expr) {
        if (expr) return;

        throw new EqlConfigException(eqlBlock.getUniqueSqlIdStr() + "'s desc format is invalid");
    }


    public static String map(final EqlRun currEqlRun, final EqlConfigDecorator eqlConfig,
                             final String sqlClassPath,
                             final CodeDesc codeDesc,
                             final String code) {
        String desc = CodeDescSettings.map(codeDesc, code);
        if (desc != null) return desc;

        final EqlBlock eqlBlock = findEqlBlock(eqlConfig, sqlClassPath, codeDesc);
        if (eqlBlock == null) return null;


        DefaultCodeDescMapper mapper = CodeDescCache.getCachedMapper(sqlClassPath, codeDesc,
                currEqlRun, eqlConfig, eqlBlock);

        return mapper == null ? null : mapper.map(code);
    }

    private static EqlBlock findEqlBlock(EqlConfigDecorator eqlConfig,
                                         String sqlClassPath,
                                         CodeDesc codeDesc) {
        try {
            EqlResourceLoader sqlResourceLoader = eqlConfig.getSqlResourceLoader();
            return sqlResourceLoader.loadEqlBlock(sqlClassPath, codeDesc.getDescLabel());
        } catch (Exception ex) {
            return null;
        }
    }
}
