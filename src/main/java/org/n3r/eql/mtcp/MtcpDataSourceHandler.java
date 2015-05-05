package org.n3r.eql.mtcp;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.google.common.cache.*;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.mtcp.utils.Mtcps;
import org.n3r.eql.util.S;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;
import static com.google.common.base.Preconditions.checkNotNull;

public class MtcpDataSourceHandler implements InvocationHandler {
    final TenantPropertiesConfigurator tenantPropertiesConfigurator;
    final EqlConfig eqlConfig;
    final ScheduledExecutorService destroyScheduler;
    final MetricRegistry metricsRegistry;
    final LoadingCache<String/*tenant id*/, DataSourceConfigurator /*tenant ds*/> mtcpCache;

    public MtcpDataSourceHandler(EqlConfig eqlConfig) {
        this.eqlConfig = eqlConfig;

        tenantPropertiesConfigurator = createMtcpTenantPropertiesConfigurator(eqlConfig);

        mtcpCache = createMtcpCache(eqlConfig);
        destroyScheduler = Executors.newSingleThreadScheduledExecutor();

        destroyScheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                mtcpCache.cleanUp();
            }
        }, 600, 600, TimeUnit.SECONDS);

        metricsRegistry = new MetricRegistry();

        metricsRegistry.register(name(MtcpDataSourceHandler.class.getSimpleName(), "cacheCount"), new Gauge<Long>() {
            @Override
            public Long getValue() {
                return mtcpCache.size();
            }
        });

        // TODO: Metric Reported should be configurated from outside.
//        ConsoleReporter reporter = ConsoleReporter.forRegistry(metricsRegistry).build();
//        reporter.start(10, TimeUnit.SECONDS);
        final Slf4jReporter reporter = Slf4jReporter.forRegistry(metricsRegistry)
                .outputTo(LoggerFactory.getLogger(MtcpDataSourceHandler.class))
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();

        reporter.start(1, TimeUnit.MINUTES);
    }

    private LoadingCache<String, DataSourceConfigurator> createMtcpCache(EqlConfig eqlConfig) {
        String key = "mtcpCacheSpec";
        String mtcpCacheSpec = eqlConfig.getStr(key);
        checkNotNull(mtcpCacheSpec, "%s should not be empty!", key);

        return CacheBuilder.from(mtcpCacheSpec)
                .removalListener(new RemovalListener<String, DataSourceConfigurator>() {
                    @Override
                    public void onRemoval(RemovalNotification<String, DataSourceConfigurator> notification) {
                        String tenantId = notification.getKey();
                        DataSourceConfigurator dataSourceConfigurator = notification.getValue();
                        dataSourceConfigurator.destory(tenantId, metricsRegistry);
                    }
                })
                .build(new CacheLoader<String, DataSourceConfigurator>() {
                    @Override
                    public DataSourceConfigurator load(String tenantId) throws Exception {
                        return createTenantDataSource(tenantId);
                    }
                });
    }

    private TenantPropertiesConfigurator createMtcpTenantPropertiesConfigurator(EqlConfig eqlConfig) {
        String key = "tenantPropertiesConfigurator.spec";
        String impl = eqlConfig.getStr(key);
        checkNotNull(impl, "%s should not be empty!", key);
        return Mtcps.createObjectBySpec(impl, TenantPropertiesConfigurator.class);
    }

    private DataSource getTenantDataSource() {
        String tenantId = MtcpContext.getTenantId();
        checkNotNull(tenantId, "there is no tenant id set in current thread local");

        return mtcpCache.getUnchecked(tenantId).getDataSource();
    }

    private DataSourceConfigurator createTenantDataSource(String tenantId) {
        String key = "dataSourceConfigurator.spec";
        String impl = eqlConfig.getStr(key);
        if (S.isBlank(impl)) impl = "@com.github.bingoohuang.mtcp.impl.DruidDataSourceConfigurator";

        DataSourceConfigurator dataSourceConfigurator = Mtcps.createObjectBySpec(impl, DataSourceConfigurator.class);

        Map<String, String> props = Mtcps.merge(eqlConfig.params(),
                tenantPropertiesConfigurator.getTenantProperties(tenantId));
        dataSourceConfigurator.prepare(tenantId, props, metricsRegistry, destroyScheduler);

        return dataSourceConfigurator;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return method.invoke(getTenantDataSource(), args);
    }

    public DataSource newMtcpDataSource() {
        ClassLoader cl = getClass().getClassLoader();
        return (DataSource) Proxy.newProxyInstance(cl, new Class[]{DataSource.class}, this);
    }

}
