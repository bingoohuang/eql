package org.n3r.eql;

import org.junit.Test;
import org.n3r.eql.diamond.Dql;
import org.n3r.eql.ex.EqlExecuteException;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class DqlTest {
    @Test
    public void test() throws InterruptedException {
        String result = new Dql().selectFirst("demo").execute();

        assertThat(result, is(notNullValue()));


//        while(true)
        {
            try {
                result = new Dql().selectFirst("demo").execute();
                System.out.println(result);
                TimeUnit.SECONDS.sleep(10);
            } catch (EqlExecuteException e) {
                e.printStackTrace();
            }
        }
    }
}
