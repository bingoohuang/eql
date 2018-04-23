package org.n3r.eql.matrix.func;

import com.google.common.collect.Lists;
import org.n3r.eql.matrix.MatrixTableFieldValue;

import java.util.List;

public class RangeFunction extends SingleFieldBaseFunction {
    private List<Tuple<Long, Long, String>> ranges = Lists.newArrayList();


    @Override
    public String apply(MatrixTableFieldValue... fieldValues) {
        long value = Long.parseLong(find(fieldValues));

        return findInRanges(value);
    }

    private String findInRanges(long value) {
        for (Tuple<Long, Long, String> tuple : ranges) {
            if (value >= tuple._1 && value < tuple._2) return tuple._3;
        }

        return "";
    }

    @Override
    public void configFunctionParameters(String... realFuncParams) {
        if (realFuncParams.length == 0) {
            throw new RuntimeException("range function need at least one param");
        }

        for (String rangeStr : realFuncParams) {
            int colonPos = rangeStr.indexOf(':');
            String rangeKey = rangeStr.substring(0, colonPos);
            String rangeVal = rangeStr.substring(colonPos + 1);

            int tildePos = rangeKey.indexOf('~');
            String from = rangeKey.substring(0, tildePos);
            String to = tildePos < rangeKey.length() - 1 ? rangeKey.substring(tildePos + 1) : "";
            long fromLong = Long.MIN_VALUE;
            long toLong = Long.MAX_VALUE;
            if (!from.equals("")) {
                fromLong = Long.parseLong(from);
            }
            if (!to.equals("")) {
                toLong = Long.parseLong(to);
            }

            ranges.add(Tuple.make(fromLong, toLong, rangeVal));
        }
    }

    private static class LongRange {
    }

    private static class Tuple<T1, T2, T3> {
        public T1 _1;
        public T2 _2;
        public T3 _3;

        public Tuple(T1 t1, T2 t2, T3 t3) {
            _1 = t1;
            _2 = t2;
            _3 = t3;
        }

        public static <T1, T2, T3> Tuple<T1, T2, T3> make(T1 t1, T2 t2, T3 t3) {
            return new Tuple<T1, T2, T3>(t1, t2, t3);
        }
    }
}
