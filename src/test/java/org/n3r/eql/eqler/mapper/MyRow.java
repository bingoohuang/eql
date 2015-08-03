package org.n3r.eql.eqler.mapper;

public class MyRow {
    String code;
    String value;

    @Override
    public String toString() {
        return "MyRow{" +
                "code='" + code + '\'' +
                ", value='" + value + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MyRow myRow = (MyRow) o;

        if (code != null ? !code.equals(myRow.code) : myRow.code != null) return false;
        return !(value != null ? !value.equals(myRow.value) : myRow.value != null);

    }

    @Override
    public int hashCode() {
        int result = code != null ? code.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
