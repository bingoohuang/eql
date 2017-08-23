package org.n3r.eql.mtcp.impl;

import com.alibaba.druid.pool.DruidDataSource;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import lombok.extern.slf4j.Slf4j;
import org.n3r.eql.mtcp.DataSourceConfigurator;
import org.n3r.eql.util.O;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import static com.codahale.metrics.MetricRegistry.name;

@Slf4j
public class DruidDataSourceConfigurator implements DataSourceConfigurator {
    DruidDataSource druidDataSource = new DruidDataSource();

    @Override
    public void prepare(String tenantId,
                        Map<String, String> props,
                        MetricRegistry metricsRegistry,
                        ScheduledExecutorService destroyScheduler) {
        createDruidDataSource(props, destroyScheduler);
        registerMetrics(tenantId, metricsRegistry);
    }

    private void createDruidDataSource(Map<String, String> props, ScheduledExecutorService destroyScheduler) {
        O.populate(druidDataSource, props);
        druidDataSource.setDestroyScheduler(destroyScheduler);
    }

    @Override
    public DataSource getDataSource() {
        return druidDataSource;
    }

    public void registerMetrics(String tenantId, MetricRegistry metricsRegistry) {
        String simpleName = DruidDataSource.class.getSimpleName();

        Gauge<Long> connectCount = new Gauge<Long>() {
            @Override
            public Long getValue() {
                return druidDataSource.getConnectCount();
            }
        };
        metricsRegistry.register(name(simpleName, tenantId, "connectCount"), connectCount);

        Gauge<Long> destroyCount = new Gauge<Long>() {
            @Override
            public Long getValue() {
                return druidDataSource.getDestroyCount();
            }
        };
        metricsRegistry.register(name(simpleName, tenantId, "destroyCount"), destroyCount);

        Gauge<Integer> activeCount = new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return druidDataSource.getActiveCount();
            }
        };
        metricsRegistry.register(name(simpleName, tenantId, "activeCount"), activeCount);

        Gauge<Integer> poolingCount = new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return druidDataSource.getPoolingCount();
            }
        };
        metricsRegistry.register(name(simpleName, tenantId, "poolingCount"), poolingCount);
    }

    @Override
    public void destory(String tenantId, MetricRegistry metricsRegistry) {
        unregisterMetrics(tenantId, metricsRegistry);
        destoryDatasource();
    }

    private void destoryDatasource() {
        try {
            druidDataSource.close();
            druidDataSource = null;
        } catch (Exception e) {
            log.error("close druidDataSource error", e);
        }
    }

    private void unregisterMetrics(String tenantId, MetricRegistry metricsRegistry) {
        String simpleName = DruidDataSource.class.getSimpleName();

        metricsRegistry.remove(name(simpleName, tenantId, "connectCount"));
        metricsRegistry.remove(name(simpleName, tenantId, "destroyCount"));
        metricsRegistry.remove(name(simpleName, tenantId, "activeCount"));
        metricsRegistry.remove(name(simpleName, tenantId, "poolingCount"));
    }
}
