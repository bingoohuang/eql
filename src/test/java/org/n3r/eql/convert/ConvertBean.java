package org.n3r.eql.convert;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/2/6.
 */
@Data @AllArgsConstructor @NoArgsConstructor
public class ConvertBean {
    private String id;
    @Strip(right = ".00")
    private String times;
    private String times2;
}
