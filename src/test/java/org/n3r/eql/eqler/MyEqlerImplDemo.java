package org.n3r.eql.eqler;

import com.google.common.collect.Maps;
import org.n3r.eql.Eql;
import org.n3r.eql.EqlPage;
import org.n3r.eql.diamond.Dql;
import org.n3r.eql.eqler.annotations.NamedParam;
import org.objectweb.asm.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public abstract class MyEqlerImplDemo implements MyEqler {
    public abstract void doSome(String some);

    public static void main(String[] args) throws IOException {
        new MyEqlerImplDemo() {

            public void doSome(String some) {

            }
        };

        Class<?> declaringClass = MyEqlerImplDemo.class;
        ClassLoader declaringClassLoader = declaringClass.getClassLoader();

        Type declaringType = Type.getType(declaringClass);
        String url = declaringType.getInternalName() + ".class";

        InputStream classFileInputStream = declaringClassLoader.getResourceAsStream(url);
        if (classFileInputStream == null) {
            throw new IllegalArgumentException(url + " is not found");
        }

        ClassReader cr = new ClassReader(classFileInputStream);
        cr.accept(new ClassVisitor(Opcodes.ASM5) {
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                System.out.println("Method:" + name);
                return new MethodVisitor(Opcodes.ASM5) {
                    // only for non-abstract or non-interface methods
                    // when code is compiled with -g or -g:vars options (generate debug variable info).
                    public void visitLocalVariable(String name, String description, String signature, Label start, Label end, int index) {
                        System.out.println(name);
                    }
                };
            }

        }, 0);
    }

    public String queryOne() {
        return new Dql("mysql").me()
                .useSqlFile(MyEqler.class)
                .id("queryOne")
                .returnType(String.class)
                .limit(1)
                .execute();
    }

    public String queryTwo() {
        return new Eql("mysql")
                .useSqlFile("org/n3r/eql/eqler/MyEqlerTwo.eql")
                .id("queryTwo")
                .returnType(EqlerFactory.class)
                .limit(10)
                .execute();
    }

    public int queryThree() {
        return new Eql("mysql")
                .useSqlFile(MyEqler.class)
                .id("queryThree")
                .returnType(int.class)
                .limit(1)
                .execute();
    }

    public long queryFour() {
        return new Eql("mysql")
                .useSqlFile(MyEqler.class)
                .id("queryFour")
                .returnType(long.class)
                .limit(40)
                .execute();
    }

    public boolean queryTrue() {
        return new Eql("mysql")
                .useSqlFile(MyEqler.class)
                .id("queryTrue")
                .returnType(boolean.class)
                .limit(1)
                .execute();
    }

    public boolean queryFalse() {
        return new Eql("mysql")
                .useSqlFile(MyEqler.class)
                .id("queryFalse")
                .returnType(boolean.class)
                .limit(1)
                .execute();
    }

    public String queryById(String id) {
        return new Eql("mysql").me()
                .useSqlFile(MyEqler.class)
                .params(id)
                .id("queryFalse")
                .returnType(String.class)
                .limit(1)
                .execute();
    }


    public String queryByMap(Map<String, String> map) {
        return new Eql("mysql")
                .useSqlFile(MyEqler.class)
                .params(map)
                .id("queryByMap")
                .returnType(String.class)
                .limit(1)
                .execute();
    }

    public Map queryByMap(long userId, String merchantId, int id, String name) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("userId", userId);
        params.put("merchantId", merchantId);
        params.put("id", id);
        params.put("name", name);

        return new Eql("mysql")
                .useSqlFile(MyEqler.class)
                .params(params)
                .id("queryByMap")
                .returnType(Map.class)
                .limit(1)
                .execute();
    }

    public MyEqlerBean queryBean(String id) {
        return new Eql("mysql")
                .useSqlFile(MyEqler.class)
                .params(id)
                .id("queryBean")
                .returnType(MyEqlerBean.class)
                .limit(1)
                .execute();
    }

    public MyEqlerBean queryBeanX(String id) {
        return new Eql("mysql")
                .useSqlFile(MyEqler.class)
                .params(id)
                .id("queryBean")
                .returnType(MyEqlerBean.class)
                .limit(1)
                .execute();
    }

    public List<MyEqlerBean> queryBeans(String id) {
        return new Eql("mysql")
                .useSqlFile(MyEqler.class)
                .params(id)
                .id("queryFalse")
                .returnType(MyEqlerBean.class)
                .execute();
    }

    public String queryDirectSql() {
        return new Eql("mysql")
                .returnType(String.class)
                .limit(1)
                .execute("select 1", "select2");
    }

    public List<MyEqlerBean> queryMoreBeans(int a, EqlPage eqlPage, int b) {
        return new Eql("mysql")
                .useSqlFile(MyEqler.class)
                .params(a, b)
                .limit(eqlPage)
                .id("queryMoreBeans")
                .returnType(MyEqlerBean.class)
                .execute();
    }

    public String queryByIds(String id1, String id2, String id3, String id4, String id5, String id6, String id7, String id8) {
        return new Eql("mysql")
                .useSqlFile(MyEqler.class)
                .params(id1, id2, id3, id4, id5, id6, id7)
                .id("queryFalse")
                .returnType(boolean.class)
                .limit(1)
                .execute();
    }
}
