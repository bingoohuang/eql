package org.n3r.eql.parser;

import lombok.Value;

@Value
public class OffsetAndOptionValue {
    private int offset;
    private String optionValue;
}
