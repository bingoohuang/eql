package org.n3r.eql.config;

public interface EqlTranFactoryCacheLifeCycle {
    void onLoad();

    void onRemoval();
}
