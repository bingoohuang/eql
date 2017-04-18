package org.n3r.eql.matrix.impl;

import lombok.Value;
import org.n3r.eql.matrix.RealPartition;

@Value
public class GotoRealPartition extends RealPartition {
    private final int gotoAnotherRule;

    public GotoRealPartition(int gotoAnotherRule) {
       this.gotoAnotherRule = gotoAnotherRule;
    }

    public int getGotoRuleNum() {
        return gotoAnotherRule;
    }
}
