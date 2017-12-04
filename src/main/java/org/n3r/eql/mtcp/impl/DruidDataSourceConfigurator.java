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
    private final DruidDataSource dataSource = new DruidDataSource();
    private String tenantId;

    @Override
    public void prepare(String tenantId,
                        Map<String, String> props,
                        MetricRegistry metricsRegistry,
                        ScheduledExecutorService destroyScheduler) {
        this.tenantId = tenantId;
        createDruidDataSource(props, destroyScheduler);
        registerMetrics(tenantId + "-" + uniqueCode, metricsRegistry);
    }

    private void createDruidDataSource(Map<String, String> props, ScheduledExecutorService destroyScheduler) {
        O.populate(dataSource, props);
        dataSource.setDestroyScheduler(destroyScheduler);
    }

    @Override
    public DataSource getDataSource() {
        return dataSource;
    }

    @Override
    public void destroy(String tenantId, MetricRegistry metricsRegistry) {
        unregisterMetrics(tenantId + "-" + uniqueCode, metricsRegistry);
        destroyDatasource();
    }

    @Override public String shrink() {
        val poolingCount = dataSource.getPoolingCount();
        val activeCount = dataSource.getActiveCount();
        log.info("get to shrink with poolingCount:{}, activeCount:{} ", poolingCount, activeCount);
        dataSource.shrink();
        val endPoolingCount = dataSource.getPoolingCount();
        val endActiveCount = dataSource.getActiveCount();
        log.info("end to shrink with poolingCount:{}, activeCount:{} ", endPoolingCount, endActiveCount);

        return "tenantId:" + tenantId + ", poolingCount:" + poolingCount + "->" + endPoolingCount
                + ", activeCount:" + activeCount + "->" + endActiveCount + ".";
    }

    private void destroyDatasource() {
        try {
            dataSource.close();
        } catch (Exception e) {
            log.error("close dataSource error", e);
        }
    }


    public void registerMetrics(String tenantPrefixedId, MetricRegistry metricsRegistry) {
        val simpleName = DruidDataSource.class.getSimpleName();
        try {
            val connectCount = new Gauge<Long>() {
                @Override
                public Long getValue() {
                    return dataSource.getConnectCount();
                }
            };
            metricsRegistry.register(name(simpleName, tenantPrefixedId, "connectCount"), connectCount);

            val destroyCount = new Gauge<Long>() {
                @Override
                public Long getValue() {
                    return dataSource.getDestroyCount();
                }
            };
            metricsRegistry.register(name(simpleName, tenantPrefixedId, "destroyCount"), destroyCount);

            val activeCount = new Gauge<Integer>() {
                @Override
                public Integer getValue() {
                    return dataSource.getActiveCount();
                }
            };
            metricsRegistry.register(name(simpleName, tenantPrefixedId, "activeCount"), activeCount);

            val poolingCount = new Gauge<Integer>() {
                @Override
                public Integer getValue() {
                    return dataSource.getPoolingCount();
                }
            };
            metricsRegistry.register(name(simpleName, tenantPrefixedId, "poolingCount"), poolingCount);
        } catch (Exception ex) {
            // ignore all exceptions
            log.warn("register metrics err", ex);
        }
    }

    private void unregisterMetrics(String tenantPrefixedId, MetricRegistry metricsRegistry) {
        val simpleName = DruidDataSource.class.getSimpleName();

        try {
            metricsRegistry.remove(name(simpleName, tenantPrefixedId, "connectCount"));
            metricsRegistry.remove(name(simpleName, tenantPrefixedId, "destroyCount"));
            metricsRegistry.remove(name(simpleName, tenantPrefixedId, "activeCount"));
            metricsRegistry.remove(name(simpleName, tenantPrefixedId, "poolingCount"));
        } catch (Exception ex) {
            // ignore all exceptions
            log.warn("unregister metrics err", ex);
        }
    }
}
