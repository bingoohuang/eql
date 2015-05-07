package org.n3r.eql.springtran.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.n3r.eql.springtran.config.EqlerConfig;
import org.n3r.eql.springtran.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by liolay on 15-5-5.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = EqlerConfig.class)
public class TestServiceTest {
    @Autowired
    private TestService testService;

    @Before
    public void before() {
        testService.prepareData();
    }


    @Test
    public void addWithTranError() {
        try {
            testService.addWithTranError();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertSame(0, testService.queryDataCount());
    }

    @Test
    public void addWithTranSuccess() {
        try {
            testService.addWithTranSuccess();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertSame(2, testService.queryDataCount());
    }
}
