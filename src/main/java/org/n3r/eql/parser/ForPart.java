package org.n3r.eql.parser;

public class ForPart implements SqlPart {
    private LiteralPart part;
    private String item;
    private String index;
    private String collection;
    private String open;
    private String seperator;
    private String close;

    public ForPart(LiteralPart part, String item, String index, String collection, String open, String seperator, String close) {
        this.part = part;
        this.item = item;
        this.index = index;
        this.collection = collection;
        this.open = open;
        this.seperator = seperator;
        this.close = close;
    }


    public LiteralPart getSqlPart() {
        return part;
    }

    @Override
    public String evalSql(Object bean) {
        return null;
    }
}
