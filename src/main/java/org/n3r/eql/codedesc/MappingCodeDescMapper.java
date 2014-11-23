package org.n3r.eql.codedesc;

import org.n3r.eql.spec.Spec;
import org.n3r.eql.spec.SpecParser;
import org.n3r.eql.util.O;

public class MappingCodeDescMapper implements CodeDescMapper {
    private final CodeDescMapper impl;

    public MappingCodeDescMapper(String valuesStr) {
        Spec spec = SpecParser.parseSpec(valuesStr);
        impl = O.createObject(CodeDescMapper.class, spec);
    }

    @Override
    public String map(String code) {
        return impl.map(code);
    }
}
