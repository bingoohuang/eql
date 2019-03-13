package org.n3r.eql.eqler.spring;


import com.github.bingoohuang.utils.spring.XyzFactoryBean;
import com.github.bingoohuang.utils.spring.XyzScannerRegistrar;
import org.n3r.eql.eqler.EqlerFactory;
import org.n3r.eql.eqler.annotations.Eqler;
import org.n3r.eql.eqler.annotations.EqlerConfig;


public class EqlerScannerRegistrar extends XyzScannerRegistrar {
    @SuppressWarnings("unchecked")
    public EqlerScannerRegistrar() {
        super(EqlerScan.class, EqlerFactoryBean.class, Eqler.class, EqlerConfig.class);
    }

    public static class EqlerFactoryBean extends XyzFactoryBean {
        public EqlerFactoryBean() {
            super(EqlerFactory::getEqler);
        }
    }
}
