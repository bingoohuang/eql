package org.n3r.eql.eqler.spring;

import org.n3r.eql.EqlPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpService {
    @Autowired SpEqler spEqler;

    public int queryOne() {
        return spEqler.queryOne();
    }

    public String queryLower() {
        return spEqler.queryLower();
    }

    public List<ABean> queryLowers() {
        return spEqler.queryLowers(new EqlPage(0, 2));
    }
}
