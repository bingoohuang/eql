package org.n3r.eql.springtran;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = EqlTransactionTestConfig.class)
public class EqlTransactionTest {
    @Autowired
    private EqlTranService eqlTranService;

    @Before
    public void before() {
        eqlTranService.prepareData();
    }

    @Test
    public void addWithTranError() {
        try {
            eqlTranService.addWithTranError();
            fail();
        } catch (ArithmeticException e) {

        }

        assertThat(eqlTranService.queryDataCount(), is(0));
    }

    @Test
    public void addWithTranSuccess() {
        eqlTranService.addWithTranSuccess();
        assertThat(eqlTranService.queryDataCount(), is(2));
    }
}
