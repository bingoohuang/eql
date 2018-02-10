package org.n3r.eql.impl.pump;

import org.n3r.eql.Eqll;
import org.n3r.eql.eqler.annotations.EqlerConfig;
import org.n3r.eql.eqler.annotations.Sql;

@EqlerConfig(eql = Eqll.class)
public interface OrderDao {
    @Sql("truncate table t_order")
    void truncateOrder();

    @Sql("insert into t_order(order_id, order_no, buyer_id, seller_id, create_time, order_desc ) "
            + "values(#orderId#, #orderNo#,  ##buyerId, #sellerId#, #createTime#, #orderDesc#);")
    void addOrder(Order order);
}

