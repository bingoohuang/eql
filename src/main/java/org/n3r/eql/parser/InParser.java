package org.n3r.eql.parser;

import java.util.List;

public class InParser implements PartParser {
    private final String inParamsContainer;

    public InParser(String inParamsContainer) {
        this.inParamsContainer = inParamsContainer;
    }

    @Override
    public EqlPart createPart() {
        return new InPart(inParamsContainer);
    }

    @Override
    public int parse(List<String> mergedLines, int index) {
        return index;
    }
}
