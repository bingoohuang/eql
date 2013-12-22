package org.n3r.eql.matrix.impl;

import org.n3r.eql.matrix.RealPartition;

import java.util.List;

public interface MatrixMapper {
    RealPartition map(String value);

    void config(List<String> mapperParams);
}
