package org.n3r.eql.map;

import java.util.Set;

public interface MapperFactory {
    default void addToDbMapper(Set<ToDbMapper> mappers) {
    }

    default void addFromDbMapper(Set<FromDbMapper> mappers) {
    }
}
