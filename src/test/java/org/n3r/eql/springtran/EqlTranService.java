package org.n3r.eql.springtran;

import org.n3r.eql.trans.spring.annotation.EqlTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EqlTranService {
    @Autowired EqlTranEqler eqlTranEqler;

    @EqlTransactional
    public void addWithTranError() {
        eqlTranEqler.addOneRecord("a");
        eqlTranEqler.addOneRecord("b");
        int i = 1 / 0;
    }

    @EqlTransactional
    public void addWithTranSuccess() {
        eqlTranEqler.addOneRecord("a");
        eqlTranEqler.addOneRecord("b");

    }

    public void addWithoutTran() {
        eqlTranEqler.addOneRecord("a");
        eqlTranEqler.addOneRecord("b");
        int i = 1 / 0;
    }

    public int queryDataCount() {
        return eqlTranEqler.queryRecordCounts();
    }

    public void prepareData() {
        eqlTranEqler.prepareData();
    }

}
