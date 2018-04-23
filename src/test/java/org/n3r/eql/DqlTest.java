package org.n3r.eql;

import org.junit.Test;
import org.n3r.diamond.client.impl.MockDiamondServer;
import org.n3r.eql.diamond.Dql;
import org.n3r.eql.ex.EqlExecuteException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class DqlTest {
    @Test
    public void test() {
        MockDiamondServer.setUpMockServer();
        MockDiamondServer.setConfigInfo("EqlConfig", "DEFAULT",
                "# 数据库连接信息\n" +
                        "url=jdbc:mysql://127.0.0.1:13306/diamond?useUnicode=true&&characterEncoding=UTF-8&connectTimeout=30000&socketTimeout=30000&autoReconnect=true\n" +
                        "username=root\n" +
                        "password=my-secret-pw\n" +
                        "\n" +
                        "connection.impl=org.n3r.eql.trans.EqlDruidConnection\n" +
                        "\n" +
                        "# 配置初始化大小、最小、最大\n" +
                        "initialSize=1\n" +
                        "minIdle=1\n" +
                        "maxActive=20\n" +
                        "\n" +
                        "# 配置获取连接等待超时的时间\n" +
                        "maxWait=1000\n" +
                        "\n" +
                        "# 配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒\n" +
                        "timeBetweenEvictionRunsMillis=60000\n" +
                        "\n" +
                        "# 配置一个连接在池中最小生存的时间，单位是毫秒\n" +
                        "minEvictableIdleTimeMillis=300000\n" +
                        "\n" +
                        "validationQuery=SELECT 'x'");
        String result = new Dql().selectFirst("demo").execute();

        assertThat(result, is(notNullValue()));


//        while(true)
        {
            try {
                result = new Dql().selectFirst("demo").execute();
//                System.out.println(result);
                // TimeUnit.SECONDS.sleep(10);
            } catch (EqlExecuteException e) {
                e.printStackTrace();
            }
        }

        MockDiamondServer.tearDownMockServer();
    }
}
