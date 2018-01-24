package org.n3r.eql;

import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Map;

import static com.google.common.truth.Truth.assertThat;

public class BenchmarkTest {
    @BeforeClass
    public static void beforeClass() {
        new Eql("h2").id("createTables").execute();
    }

    @Benchmark
    public void benchmark() {
        Map<String, Object> map = Maps.newHashMap();
        map.put("userId", "0");
        map.put("courseTypeId", "0");
        map.put("coachId", "0");
        map.put("startDate", DateTime.now());
        map.put("endDate", DateTime.now());

        Object obj = new Eql("h2").params(map).execute();
        assertThat(obj).isNotNull();
    }


    @Test
    public void bench() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkTest.class.getSimpleName())
                .forks(0)
                .warmupIterations(10)
                .measurementIterations(10)
                .threads(20)
                .build();

        new Runner(opt).run();
    }
}
