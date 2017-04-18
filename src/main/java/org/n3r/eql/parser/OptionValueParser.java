package org.n3r.eql.parser;

public interface OptionValueParser {
    String getKey();

    OffsetAndOptionValue parseValueOption(String valueStr);
}
