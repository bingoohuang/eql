package org.n3r.eql.eqler;

public class MyEqlerBean {
    private String a;

    public MyEqlerBean() {
    }

    public MyEqlerBean(String a) {
        this.a = a;
    }

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    @Override
    public String toString() {
        return "MyEqlerBean{" +
                "a='" + a + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MyEqlerBean that = (MyEqlerBean) o;

        return !(a != null ? !a.equals(that.a) : that.a != null);
    }

    @Override
    public int hashCode() {
        return a != null ? a.hashCode() : 0;
    }
}
