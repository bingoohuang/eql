package org.n3r.eql.parser;

import lombok.Value;

import java.util.List;

@Value
public class InParser implements PartParser {
    private final String inParamsContainer;

    @Override
    public EqlPart createPart() {
        return new InPart(inParamsContainer);
    }

    @Override
    public int parse(List<String> mergedLines, int index) {
        return index;
    }
}
