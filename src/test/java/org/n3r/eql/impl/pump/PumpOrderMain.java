package org.n3r.eql.impl.pump;

import joptsimple.OptionParser;
import lombok.val;
import org.n3r.eql.Eql;
import org.n3r.eql.Eqll;
import org.n3r.eql.config.EqlJdbcConfig;
import org.n3r.eql.eqler.EqlerFactory;
import org.n3r.eql.impl.EqlBatch;
import org.n3r.idworker.Sid;

import java.io.IOException;
import java.util.Random;

public class PumpOrderMain {
/*
## Pumping into mysql table for big table test.

```
~/g/alligator > java -jar target/alligator-0.0.1.jar -h
Option                  Description
------                  -----------
--batchNum [Integer]    每个批次数量 (default: 1)
--batchSize [Integer]   几个批次 (default: 10)
--batchStart [Integer]  批次开始序号 (default: 0)
--help                  show help
--mysqlAddr             MySQLl连接地址 (default: 192.168.99.100:13306)
```
 */
    public static void main(String[] args) throws IOException {
        new PumpOrderMain().pump(args);
    }

    String mysqlAddr;
    Integer batchSize;
    Integer batchNum;
    Integer batchStart;
    EqlJdbcConfig dbaConfig;

    private void pump(String[] args) throws IOException {
        parseArgs(args);

        // pumpByDao();
        pumpByBatch();
    }

    private void pumpByBatch() {
        truncateOrderAtFirstBatch();

        val random = new Random();

        for (int i = batchStart, ii = batchStart + batchNum; i < ii; ++i) {
            val startMillis = System.currentTimeMillis();

            batchPump(random, i);

            val endMillis = System.currentTimeMillis();

            System.out.println("Batch:" + i + ", BatchSize:" + batchSize
                    + ", Cost:" + PumpPersonMain.readableDuration(endMillis - startMillis));
        }
    }

    private void truncateOrderAtFirstBatch() {
        Eqll.choose(dbaConfig);

        if (batchStart == 0) {
            val personDao = EqlerFactory.getEqler(OrderDao.class);
            personDao.truncateOrder();
        }
    }

    private void batchPump(Random random, int batchNo) {
        val eqlTran = new Eql(dbaConfig).newTran();
        eqlTran.start();
        val eqlBatch = new EqlBatch(batchSize);

        for (int j = 0; j < batchSize; ++j) {
            int orderId = batchNo * batchSize + j;
            String orderNo = Sid.nextShort();
            long buyerId = Math.abs(random.nextLong());
            long sellerId = Math.abs(random.nextLong());
            String orderDesc = "大蓝鲸人" + j;
            new Eql(dbaConfig)
                    .useBatch(eqlBatch).useTran(eqlTran)
                    .params(orderId, orderNo, buyerId, sellerId, orderDesc)
                    .execute("insert into t_order(order_id, order_no, "
                            + "buyer_id, seller_id, create_time, order_desc) "
                            + "values(##, ##,  ##, ##, now(), ##)");
        }

        eqlBatch.executeBatch();
        eqlTran.commit();
    }

    private void parseArgs(String[] args) throws IOException {
        val parser = new OptionParser();
        val helpOption = parser.accepts("help", "show help").forHelp();
        val batchSizeOption = parser.accepts("batchSize", "几个批次")
                .withOptionalArg().ofType(Integer.class).defaultsTo(10);
        val batchNumOption = parser.accepts("batchNum", "每个批次数量")
                .withOptionalArg().ofType(Integer.class).defaultsTo(1);
        val batchStartOption = parser.accepts("batchStart", "批次开始序号")
                .withOptionalArg().ofType(Integer.class).defaultsTo(0);
        val mysqlAddrOption = parser.accepts("mysqlAddr", "MySQLl连接地址")
                .withOptionalArg().ofType(String.class).defaultsTo("192.168.99.100:13306");

        val options = parser.parse(args);
        if (options.has(helpOption)) {
            parser.printHelpOn(System.out);
            System.exit(0);
        }

        batchSize = batchSizeOption.value(options);
        batchNum = batchNumOption.value(options);
        batchStart = batchStartOption.value(options);
        mysqlAddr = mysqlAddrOption.value(options);

        dbaConfig = new EqlJdbcConfig(
                "com.mysql.cj.jdbc.Driver",
                "jdbc:mysql://" + mysqlAddr + "/dba?useSSL=false" +
                        "&useUnicode=true&characterEncoding=UTF-8" +
                        "&connectTimeout=3000&socketTimeout=3000&autoReconnect=true",
                "root", "my-secret-pw");
    }
}
