package org.n3r.eql.mtcp;

import com.codahale.metrics.MetricRegistry;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

public interface DataSourceConfigurator {
    /**
     * 准备连接池配置器
     *
     * @param tenantId         租户ID
     * @param props            租户连接池属性
     * @param metricsRegistry  连接池指标注册器
     * @param destroyScheduler 连接池空闲连接销毁排程器
     */
    void prepare(String tenantId, Map<String, String> props,
                 MetricRegistry metricsRegistry,
                 ScheduledExecutorService destroyScheduler);

    /**
     * 获取连接池
     *
     * @return 连接池
     */
    DataSource getDataSource();

    /**
     * 销毁连接池
     *
     * @param tenantId        商户id
     * @param metricsRegistry 度量注册器
     */
    void destroy(String tenantId, MetricRegistry metricsRegistry);


    /**
     * 缩减连接池空闲连接.
     * @return shrink message
     */
    String shrink();

}
