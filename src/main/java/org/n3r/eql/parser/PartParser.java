package org.n3r.eql.parser;

import java.util.List;

public interface PartParser {
    EqlPart createPart();

    int parse(List<String> mergedLines, int index);
}
