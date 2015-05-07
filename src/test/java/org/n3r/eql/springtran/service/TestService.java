package org.n3r.eql.springtran.service;

import org.n3r.eql.springtran.dao.TestDao;
import org.n3r.eql.trans.spring.annotation.EqlTranactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by liolay on 15-5-5.
 */
@Service
public class TestService {
    @Autowired
    private TestDao testDao;

    @EqlTranactional
    public void addWithTranError() {
        testDao.addOneRecord("a");
        testDao.addOneRecord("b");
        int i = 1 / 0;

    }

    @EqlTranactional
    public void addWithTranSuccess() {
        testDao.addOneRecord("a");
        testDao.addOneRecord("b");

    }

    public void addWithoutTran() {
        testDao.addOneRecord("a");
        testDao.addOneRecord("b");
        int i = 1 / 0;
    }

    public int queryDataCount() {
        return testDao.queryRecordCounts();
    }

    public void prepareData() {
        testDao.prepareData();
    }


}
