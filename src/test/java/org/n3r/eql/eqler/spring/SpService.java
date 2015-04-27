package org.n3r.eql.eqler.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SpService {
    @Autowired
    SpEqler spEqler;

    public int queryOne() {
        return spEqler.queryOne();
    }
}
