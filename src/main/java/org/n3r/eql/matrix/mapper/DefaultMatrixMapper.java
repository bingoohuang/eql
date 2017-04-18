package org.n3r.eql.matrix.mapper;

import com.google.common.collect.Lists;
import lombok.Value;
import org.n3r.eql.matrix.RealPartition;
import org.n3r.eql.matrix.impl.GotoRealPartition;
import org.n3r.eql.matrix.impl.MatrixMapper;
import org.n3r.eql.util.S;

import java.util.List;

public class DefaultMatrixMapper implements MatrixMapper {
    private String defaultValue;
    private List<MapCase> mapCases = Lists.<MapCase>newArrayList();
    private int gotoAnotherRule = -1;

    @Override
    public RealPartition map(String value) {
        for (MapCase mapCase : mapCases) {
            if (mapCase.left.equals(value))
                return RealPartition.parse(mapCase.right);
        }

        if (gotoAnotherRule >= 0) {
            return new GotoRealPartition(gotoAnotherRule);
        }

        return RealPartition.parse(defaultValue.replaceAll("\\$", value));
    }

    @Override
    public void config(List<String> mapperParams) {
        int size = mapperParams.size();
        if (size == 0) {
            throw new RuntimeException("mapper is invalid");
        }

        for (int i = 0, ii = size; i < ii; ++i) {
            String mapCase = mapperParams.get(i);
            int colonPos = mapCase.indexOf(':');
            if (colonPos < 0) { // not found
                if (i == ii - 1) {
                    if (mapCase.startsWith("rule->")) {
                        gotoAnotherRule = Integer.parseInt(mapCase.substring("rule->".length()));
                    } else {
                        defaultValue = mapCase;
                    }
                    break;
                } else {
                    throw new RuntimeException("mapper is invalid");
                }
            }

            if (colonPos == 0 || colonPos == mapCase.length() - 1) {
                throw new RuntimeException("mapper is invalid");
            }

            String left = mapCase.substring(0, colonPos);
            String right = S.trimLeft(mapCase.substring(colonPos + 1));

            mapCases.add(new MapCase(left, right));
        }
    }

    @Value
    private static class MapCase {
        public final String left;
        public final String right;
    }
}
