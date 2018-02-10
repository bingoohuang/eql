package org.n3r.eql.impl.pump;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class Order {
    private long orderId;
    private String orderNo;
    private long buyerId;
    private long sellerId;
    private Timestamp createTime;
    private String orderDesc;
}
