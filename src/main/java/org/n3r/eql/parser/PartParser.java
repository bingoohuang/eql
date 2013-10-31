package org.n3r.eql.parser;

import java.util.List;

public interface PartParser {
    SqlPart createPart();

    int parse(List<String> mergedLines, int index);
}
