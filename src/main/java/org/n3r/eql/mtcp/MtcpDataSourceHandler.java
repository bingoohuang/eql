package org.n3r.eql.mtcp;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.google.common.base.MoreObjects;
import com.google.common.cache.*;
import com.google.common.io.Files;
import lombok.SneakyThrows;
import lombok.experimental.var;
import lombok.val;
import org.apache.commons.codec.Charsets;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.mtcp.utils.Mtcps;
import org.n3r.eql.util.EqlUtils;
import org.n3r.eql.util.S;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;
import static com.google.common.base.Preconditions.checkNotNull;

public class MtcpDataSourceHandler implements InvocationHandler {
    final TenantPropertiesConfigurator tenantPropertiesConfigurator;
    final EqlConfig eqlConfig;
    final ScheduledExecutorService scheduler;
    final MetricRegistry metricsRegistry;
    final LoadingCache<String/*tenant id*/, DataSourceConfigurator /*tenant ds*/> mtcpCache;

    public MtcpDataSourceHandler(EqlConfig eqlConfig) {
        this.eqlConfig = eqlConfig;

        tenantPropertiesConfigurator = createMtcpTenantPropertiesConfigurator(eqlConfig);

        mtcpCache = createMtcpCache(eqlConfig);
        scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                mtcpCache.cleanUp();
            }
        }, 10, 10, TimeUnit.MINUTES);

        scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override public void run() {
                shrink();
            }
        }, 10, 10, TimeUnit.SECONDS);

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

    private static final String SHRINK_NOW_FILE_NAME = EqlUtils.expandUserHome("~/.eql/shrink.now");

    @SneakyThrows
    private void shrink() {
        val shrinkNowFile = new File(SHRINK_NOW_FILE_NAME);
        if (!shrinkNowFile.exists() || !shrinkNowFile.isFile()) return;

        val simpleDateFormat = new SimpleDateFormat(".yyyyMMDDHHmmss.SSS");
        val dest = new File(SHRINK_NOW_FILE_NAME + simpleDateFormat.format(new Date()));
        shrinkNowFile.renameTo(dest);

        val configurators = mtcpCache.asMap().values();
        val stat = new StringBuilder();
        int shrunk = 0;
        for (val configurator : configurators) {
            ++shrunk;
            stat.append(configurator.shrink()).append(EqlUtils.LINE_SEPARATOR);
        }

        stat.append("Total " + shrunk + "/" + mtcpCache.size()
                + " were shrunk.").append(EqlUtils.LINE_SEPARATOR);

        Files.write(stat.toString(), dest, Charsets.UTF_8);
    }

    private LoadingCache<String, DataSourceConfigurator> createMtcpCache(EqlConfig eqlConfig) {
        val key = "mtcpCacheSpec";
        val mtcpCacheSpec = eqlConfig.getStr(key);
        checkNotNull(mtcpCacheSpec, "%s should not be empty!", key);

        return CacheBuilder.from(mtcpCacheSpec)
                .removalListener(new RemovalListener<String, DataSourceConfigurator>() {
                    @Override
                    public void onRemoval(RemovalNotification<String, DataSourceConfigurator> notification) {
                        val tenantId = notification.getKey();
                        val dataSourceConfigurator = notification.getValue();
                        dataSourceConfigurator.destroy(tenantId, metricsRegistry);
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
        val key = "tenantPropertiesConfigurator.spec";
        val impl = eqlConfig.getStr(key);
        checkNotNull(impl, "%s should not be empty!", key);
        return Mtcps.createObjectBySpec(impl, TenantPropertiesConfigurator.class);
    }

    private DataSource getTenantDataSource() {
        val tenantId = getTenantId();
        checkNotNull(tenantId, "there is no tenant id set in current thread local");

        return mtcpCache.getUnchecked(tenantId).getDataSource();
    }

    protected String getTenantId() {
        return MoreObjects.firstNonNull(MtcpContext.getGroupTenantId(), MtcpContext.getTenantId());
    }

    private DataSourceConfigurator createTenantDataSource(String tenantId) {
        val key = "dataSourceConfigurator.spec";
        var impl = eqlConfig.getStr(key);
        if (S.isBlank(impl))
            impl = "@org.n3r.eql.mtcp.impl.DruidDataSourceConfigurator";

        val dataSourceConfigurator = Mtcps.createObjectBySpec(impl, DataSourceConfigurator.class);

        val props = Mtcps.merge(eqlConfig.params(), tenantPropertiesConfigurator.getTenantProperties(tenantId));
        dataSourceConfigurator.prepare(tenantId, props, metricsRegistry, scheduler);

        return dataSourceConfigurator;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return method.invoke(getTenantDataSource(), args);
    }

    public DataSource newMtcpDataSource() {
        val cl = getClass().getClassLoader();
        return (DataSource) Proxy.newProxyInstance(cl, new Class[]{DataSource.class}, this);
    }

}
