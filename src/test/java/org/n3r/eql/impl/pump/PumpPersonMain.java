package org.n3r.eql.impl.pump;

import joptsimple.OptionParser;
import lombok.val;
import org.n3r.eql.Eql;
import org.n3r.eql.Eqll;
import org.n3r.eql.config.EqlJdbcConfig;
import org.n3r.eql.eqler.EqlerFactory;
import org.n3r.eql.impl.EqlBatch;

import java.util.concurrent.TimeUnit;

public class PumpPersonMain {
    public static void main(String[] args) {
        new PumpPersonMain().pump(args);
    }

    Integer batchSize;
    Integer batchNum;

    private void pump(String[] args) {
        parseArgs(args);

        // pumpByDao();
        pumpByBatch();
    }

    EqlJdbcConfig dbaConfig = new EqlJdbcConfig(
            "com.mysql.cj.jdbc.Driver",
            "jdbc:mysql://192.168.99.100:13306/dba?useSSL=false" +
                    "&useUnicode=true&characterEncoding=UTF-8" +
                    "&connectTimeout=3000&socketTimeout=3000&autoReconnect=true",
            "root", "my-secret-pw");

    private void pumpByBatch() {
        Eqll.choose(dbaConfig);

        val personDao = EqlerFactory.getEqler(PersonDao.class);

        personDao.truncatePerson();

        for (int i = 0; i < batchNum; ++i) {
            val startMillis = System.currentTimeMillis();

            val eqlTran = new Eql(dbaConfig).newTran();
            eqlTran.start();
            val eqlBatch = new EqlBatch(batchSize);

            for (int j = 0; j < batchSize; ++j)
                new Eql(dbaConfig).useBatch(eqlBatch).useTran(eqlTran)
                        .params(i + "x" + j, "bingoo" + i + "x" + j, 1, "大蓝鲸人")
                        .execute("insert into person(id, name, sex, addr) values(##, ##, ##, ##)");

            eqlBatch.executeBatch();
            eqlTran.commit();

            val endMillis = System.currentTimeMillis();

            System.out.println("Batch:" + i + ", BatchSize:" + batchSize
                    + ", Cost:" + readableDuration(endMillis - startMillis));
        }
    }

    private void pumpByDao() {
        Eqll.choose(dbaConfig);

        val personDao = EqlerFactory.getEqler(PersonDao.class);

        personDao.truncatePerson();

        for (int i = 0; i < batchNum; ++i) {
            val startMillis = System.currentTimeMillis();

            for (int j = 0; j < batchSize; ++j)
                personDao.addPerson(Person.create(i + "x" + j, "bingoo" + i + "x" + j, "大蓝鲸人", 1));

            val endMillis = System.currentTimeMillis();

            System.out.println("Batch:" + i + ", BatchSize:" + batchSize
                    + ", Cost:" + readableDuration(endMillis - startMillis));
        }
    }

    /**
     * Convert a millisecond duration to a string format
     *
     * @param millis A duration to convert to a string form
     * @return A string of the form "X Days Y Hours Z Minutes A Seconds".
     */
    public static String readableDuration(long millis) {
        if (millis < 0)
            throw new IllegalArgumentException("Duration must be greater than zero!");

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder(64);
        if (days > 0) sb.append(days).append(" Days ");
        if (hours > 0) sb.append(hours).append(" Hours ");
        if (minutes > 0) sb.append(minutes).append(" Minutes ");
        sb.append(seconds).append(" Seconds");

        return sb.toString().trim();
    }

    private void parseArgs(String[] args) {
        val parser = new OptionParser();
        val batchSizeOption = parser.accepts("batchSize")
                .withOptionalArg().ofType(Integer.class).defaultsTo(100000);
        val batchNumOption = parser.accepts("batchNum")
                .withOptionalArg().ofType(Integer.class).defaultsTo(1000);

        val options = parser.parse(args);
        batchSize = batchSizeOption.value(options);
        batchNum = batchNumOption.value(options);
    }
}
