package org.n3r.eql.parser;

public class OffsetAndOptionValue {
    private int offset;
    private String optionValue;

    public OffsetAndOptionValue(int offset, String optionValue) {
        this.offset = offset;
        this.optionValue = optionValue;
    }

    public int getOffset() {
        return offset;
    }

    public String getOptionValue() {
        return optionValue;
    }
}
