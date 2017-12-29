package org.n3r.eql.codedesc;

import lombok.SneakyThrows;
import lombok.val;
import org.n3r.eql.config.EqlConfigDecorator;
import org.n3r.eql.ex.EqlConfigException;
import org.n3r.eql.map.EqlRun;
import org.n3r.eql.parser.EqlBlock;
import org.n3r.eql.spec.SpecParser;
import org.n3r.eql.util.Rs;
import org.n3r.eql.util.S;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CodeDescs {
    public static ResultSet codeDescWrap(
            EqlRun currEqlRun, EqlBlock eqlBlock,
            EqlConfigDecorator eqlConfig,
            String sqlClassPath, ResultSet rs, String tagSqlId) {
        val descs = eqlBlock.getCodeDescs();
        if (descs == null) return rs;

        if (!existsReturnDescColumns(descs, rs)) return rs;

        return new CodeDescResultSetHandler(currEqlRun, eqlConfig,
                sqlClassPath, rs, descs, tagSqlId).createProxy();
    }

    @SneakyThrows
    private static boolean existsReturnDescColumns(List<CodeDesc> descs, ResultSet rs) {
        val metaData = rs.getMetaData();
        for (int i = 0, ii = metaData.getColumnCount(); i < ii; ++i) {
            val columnName = Rs.lookupColumnName(metaData, i + 1);

            for (val codeDesc : descs) {
                if (codeDesc.getColumnName().equals(columnName)) return true;
            }
        }

        return false;
    }

    public static List<CodeDesc> parseOption(EqlBlock eqlBlock, String desc) {
        if (S.isBlank(desc)) return null;

        val codeDescs = new ArrayList<CodeDesc>();

        val descOptionValueParser = new DescOptionValueParser();
        int pos = 0;
        int size = desc.length();
        while (pos < size) {
            val oo = descOptionValueParser.parseValueOption(desc.substring(pos));
            if (oo == null) break;

            pos += oo.getOffset();

            val codeDesc = parseCodeDesc(eqlBlock, oo.getOptionValue());
            codeDescs.add(codeDesc);
        }

        return codeDescs.size() == 0 ? null : codeDescs;
    }

    private static CodeDesc parseCodeDesc(EqlBlock eqlBlock, String descPart) {
        int atPos = descPart.indexOf('@');
        check(eqlBlock, atPos > 0);
        val columnName = descPart.substring(0, atPos);
        val reference = descPart.substring(atPos);
        val spec = SpecParser.parseSpec(reference);

        return new CodeDesc(columnName, spec);
    }

    private static void check(EqlBlock eqlBlock, boolean expr) {
        if (expr) return;

        throw new EqlConfigException(eqlBlock.getUniqueSqlIdStr()
                + "'s desc format is invalid");
    }


    public static String map(final EqlRun currEqlRun,
                             final EqlConfigDecorator eqlConfig,
                             final String sqlClassPath,
                             final CodeDesc codeDesc,
                             final String code,
                             String tagSqlId) {
        val desc = CodeDescSettings.map(codeDesc, code);
        if (desc != null) return desc;

        val eqlBlock = findEqlBlock(eqlConfig, sqlClassPath, codeDesc);
        if (eqlBlock == null) return null;


        val mapper = CodeDescCache.getCachedMapper(sqlClassPath, codeDesc,
                currEqlRun, eqlConfig, eqlBlock, tagSqlId);

        return mapper == null ? null : mapper.map(code);
    }

    private static EqlBlock findEqlBlock(EqlConfigDecorator eqlConfig,
                                         String sqlClassPath,
                                         CodeDesc codeDesc) {
        try {
            val sqlResourceLoader = eqlConfig.getSqlResourceLoader();
            return sqlResourceLoader.loadEqlBlock(sqlClassPath, codeDesc.getDescLabel());
        } catch (Exception ex) {
            return null;
        }
    }
}
