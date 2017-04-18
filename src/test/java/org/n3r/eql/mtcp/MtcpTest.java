package org.n3r.eql.mtcp;

import org.n3r.eql.eqler.EqlerFactory;

import java.security.SecureRandom;
import java.util.Random;

public class MtcpTest {

    public static void main(String[] args) {
        MtcpEqler mtcpEqler = EqlerFactory.getEqler(MtcpEqler.class);

        Random random = new SecureRandom();
        while (true) {
            int i = random.nextInt(5);
            if (i == 1) {
                MtcpContext.setTenantId("mtcp-dba");
                mtcpEqler.queryCnt();
            } else if (i == 2) {
                MtcpContext.setTenantId("mtcp-dbb");
                mtcpEqler.queryCnt();
            } else if (i == 3) {
                MtcpContext.setTenantId("mtcp-dbc");
                mtcpEqler.queryCnt();
            } else if (i == 4) {
                MtcpContext.setTenantId("mtcp-diamond");
                mtcpEqler.queryCnt();
            }

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }
}
