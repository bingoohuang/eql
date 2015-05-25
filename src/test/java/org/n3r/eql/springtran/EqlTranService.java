package org.n3r.eql.springtran;

import org.n3r.eql.trans.spring.annotation.EqlTranactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class EqlTranService {
    @Autowired
    private EqlTranEqler eqlTranEqler;

    @EqlTranactional
    public void addWithTranError() {
        eqlTranEqler.addOneRecord("a");
        eqlTranEqler.addOneRecord("b");
        int i = 1 / 0;
    }

    @EqlTranactional
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
