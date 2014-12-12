package org.n3r.eql.base;

import org.n3r.eql.parser.EqlBlock;
import org.n3r.eql.parser.Sql;

import java.util.List;

public interface DynamicLanguageDriver {
    Sql parse(EqlBlock block, List<String> onEQLLines);
}
