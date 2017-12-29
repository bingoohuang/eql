package org.n3r.eql.config;

import lombok.val;
import org.n3r.diamond.client.DiamondListenerAdapter;
import org.n3r.diamond.client.DiamondManager;
import org.n3r.diamond.client.DiamondStone;
import org.n3r.diamond.client.Miner;
import org.n3r.eql.impl.DefaultEqlConfigDecorator;

public class EqlDiamondConfig extends EqlPropertiesConfig
        implements EqlTranFactoryCacheLifeCycle {
    public static final String EQL_CONFIG_GROUP_NAME = "EqlConfig";

    private String connectionName;
    private DiamondListenerAdapter diamondListener;
    private DiamondManager diamondManager;

    public EqlDiamondConfig(String connectionName) {
        super(new Miner().getProperties(EQL_CONFIG_GROUP_NAME, connectionName));
        this.connectionName = connectionName;
    }

    @Override
    public void onLoad() {
        diamondManager = new DiamondManager(EQL_CONFIG_GROUP_NAME, connectionName);
        val eqlConfig = new DefaultEqlConfigDecorator(this);
        diamondListener = new DiamondListenerAdapter() {
            @Override
            public void accept(DiamondStone diamondStone) {
                EqlConfigManager.invalidateCache(eqlConfig);
            }
        };

        diamondManager.addDiamondListener(diamondListener);
    }

    @Override
    public void onRemoval() {
        diamondManager.removeDiamondListener(diamondListener);
    }
}
