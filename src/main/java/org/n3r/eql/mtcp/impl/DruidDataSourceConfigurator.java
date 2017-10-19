package org.n3r.eql.mtcp.impl;

import com.alibaba.druid.pool.DruidDataSource;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.n3r.eql.mtcp.DataSourceConfigurator;
import org.n3r.eql.util.O;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static com.codahale.metrics.MetricRegistry.name;

@Slf4j
public class DruidDataSourceConfigurator implements DataSourceConfigurator {
    private static final AtomicInteger uniqueCodeGenerator = new AtomicInteger(0);

    private final int uniqueCode = uniqueCodeGenerator.incrementAndGet();
    private final DruidDataSource druidDataSource = new DruidDataSource();

    @Override
    public void prepare(String tenantId,
                        Map<String, String> props,
                        MetricRegistry metricsRegistry,
                        ScheduledExecutorService destroyScheduler) {
        createDruidDataSource(props, destroyScheduler);
        registerMetrics(tenantId + "-" + uniqueCode, metricsRegistry);
    }

    private void createDruidDataSource(Map<String, String> props, ScheduledExecutorService destroyScheduler) {
        O.populate(druidDataSource, props);
        druidDataSource.setDestroyScheduler(destroyScheduler);
    }

    @Override
    public DataSource getDataSource() {
        return druidDataSource;
    }

    public void registerMetrics(String tenantPrefixedId, MetricRegistry metricsRegistry) {
        val simpleName = DruidDataSource.class.getSimpleName();

        val connectCount = new Gauge<Long>() {
            @Override
            public Long getValue() {
                return druidDataSource.getConnectCount();
            }
        };
        metricsRegistry.register(name(simpleName, tenantPrefixedId, "connectCount"), connectCount);

        val destroyCount = new Gauge<Long>() {
            @Override
            public Long getValue() {
                return druidDataSource.getDestroyCount();
            }
        };
        metricsRegistry.register(name(simpleName, tenantPrefixedId, "destroyCount"), destroyCount);

        val activeCount = new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return druidDataSource.getActiveCount();
            }
        };
        metricsRegistry.register(name(simpleName, tenantPrefixedId, "activeCount"), activeCount);

        val poolingCount = new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return druidDataSource.getPoolingCount();
            }
        };
        metricsRegistry.register(name(simpleName, tenantPrefixedId, "poolingCount"), poolingCount);
    }

    @Override
    public void destory(String tenantId, MetricRegistry metricsRegistry) {
        unregisterMetrics(tenantId + "-" + uniqueCode, metricsRegistry);
        destoryDatasource();
    }

    private void destoryDatasource() {
        try {
            druidDataSource.close();
        } catch (Exception e) {
            log.error("close druidDataSource error", e);
        }
    }

    private void unregisterMetrics(String tenantPrefixedId, MetricRegistry metricsRegistry) {
        val simpleName = DruidDataSource.class.getSimpleName();

        metricsRegistry.remove(name(simpleName, tenantPrefixedId, "connectCount"));
        metricsRegistry.remove(name(simpleName, tenantPrefixedId, "destroyCount"));
        metricsRegistry.remove(name(simpleName, tenantPrefixedId, "activeCount"));
        metricsRegistry.remove(name(simpleName, tenantPrefixedId, "poolingCount"));
    }
}
