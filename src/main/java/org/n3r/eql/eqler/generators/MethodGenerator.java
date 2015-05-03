package org.n3r.eql.eqler.generators;

import org.n3r.eql.EqlTranable;
import org.n3r.eql.eqler.annotations.*;
import org.n3r.eql.util.S;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.objectweb.asm.Opcodes.*;

public class MethodGenerator<T> {
    public static final String EQL = "org/n3r/eql/Eql";
    private final Method method;
    private final MethodVisitor mv;
    private final Class<T> eqlerClass;
    private final EqlerConfig eqlerConfig;
    private final UseSqlFile classUseSqlFile;
    private final String eqlClassName;
    private final MethodAllParam methodAllParam;


    public MethodGenerator(ClassWriter classWriter, Method method, Class<T> eqlerClass) {
        this.method = method;
        this.eqlerClass = eqlerClass;
        EqlerConfig eqlerConfig = method.getAnnotation(EqlerConfig.class);
        this.eqlerConfig = eqlerConfig != null ? eqlerConfig : eqlerClass.getAnnotation(EqlerConfig.class);
        this.eqlClassName = this.eqlerConfig != null ? Type.getInternalName(this.eqlerConfig.eql()) : EQL;
        this.classUseSqlFile = eqlerClass.getAnnotation(UseSqlFile.class);
        this.mv = classWriter.visitMethod(ACC_PUBLIC, method.getName(), Type.getMethodDescriptor(method), null, null);
        this.methodAllParam = parseParams(method);
    }

    public void generate() {
        start();

        prepareNamedParams();
        prepareNamedDynamics();

        newEql();
        useTran();
        useBatch();
        useSqlFile();
        params();
        dynamics();
        id();
        returnType();
        limit();
        execute();
        result();

        end();
    }

    private void useBatch() {
        MethodParam eqlBatch = methodAllParam.getEqlBatch();
        if (eqlBatch == null) return;

        mv.visitVarInsn(ALOAD, eqlBatch.getParamIndex() + 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, EQL, "useBatch", "(Lorg/n3r/eql/impl/EqlBatch;)Lorg/n3r/eql/Eql;", false);
    }

    private void useTran() {
        MethodParam eqlTran = methodAllParam.getEqlTran();
        if (eqlTran != null) {
            mv.visitVarInsn(ALOAD, eqlTran.getParamIndex() + 1);
            mv.visitMethodInsn(INVOKEVIRTUAL, EQL, "useTran", "(Lorg/n3r/eql/EqlTran;)Lorg/n3r/eql/Eql;", false);
        } else if (EqlTranable.class.isAssignableFrom(eqlerClass)) {
            mv.visitMethodInsn(INVOKESTATIC, "org/n3r/eql/trans/EqlTranThreadLocal", "get", "()Lorg/n3r/eql/EqlTran;", false);
            mv.visitMethodInsn(INVOKEVIRTUAL, EQL, "useTran", "(Lorg/n3r/eql/EqlTran;)Lorg/n3r/eql/Eql;", false);
        }
    }

    private void prepareNamedParams() {
        if (methodAllParam.getNamedParamsCount() == 0) return;

        mv.visitMethodInsn(INVOKESTATIC, "com/google/common/collect/Maps", "newHashMap", "()Ljava/util/HashMap;", false);

        mv.visitVarInsn(ASTORE, methodAllParam.getAsmLocalVarNamedParamIndex());

        for (int i = 0, incrs = 0; i < methodAllParam.getMethodParamsCount(); ++i) {
            MethodParam methodParam = methodAllParam.getMethodParam(i);
            Param param = methodParam.getParam();
            if (param == null) continue;

            mv.visitVarInsn(ALOAD, methodAllParam.getAsmLocalVarNamedParamIndex());
            mv.visitLdcInsn(param.value());

            incrs += visitVar(i + 1 + incrs, Type.getType(methodParam.getParamType()));
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true);
            mv.visitInsn(POP);
        }
    }

    private void prepareNamedDynamics() {
        if (methodAllParam.getNamedDynamicCount() == 0) return;

        mv.visitMethodInsn(INVOKESTATIC, "com/google/common/collect/Maps", "newHashMap", "()Ljava/util/HashMap;", false);

        mv.visitVarInsn(ASTORE, methodAllParam.getAsmLocalVarNamedDynamicIndex());

        for (int i = 0, incrs = 0; i < methodAllParam.getMethodParamsCount(); ++i) {
            MethodParam methodParam = methodAllParam.getMethodParam(i);
            Dynamic dynamic = methodParam.getDynamic();
            if (dynamic == null) continue;
            if (isBlank(dynamic.name())) continue;

            mv.visitVarInsn(ALOAD,  methodAllParam.getAsmLocalVarNamedDynamicIndex());
            mv.visitLdcInsn(dynamic.name());

            incrs += visitVar(i + 1 + incrs, Type.getType(methodParam.getParamType()));
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true);
            mv.visitInsn(POP);
        }
    }

    private int visitVar(int i, Type tp) {
        if (tp.equals(Type.BOOLEAN_TYPE)) {
            mv.visitVarInsn(ILOAD, i);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
        } else if (tp.equals(Type.BYTE_TYPE)) {
            mv.visitVarInsn(ILOAD, i);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false);
        } else if (tp.equals(Type.CHAR_TYPE)) {
            mv.visitVarInsn(ILOAD, i);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false);
        } else if (tp.equals(Type.SHORT_TYPE)) {
            mv.visitVarInsn(ILOAD, i);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false);
        } else if (tp.equals(Type.INT_TYPE)) {
            mv.visitVarInsn(ILOAD, i);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
        } else if (tp.equals(Type.LONG_TYPE)) {
            mv.visitVarInsn(LLOAD, i);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
            return 1;
        } else if (tp.equals(Type.FLOAT_TYPE)) {
            mv.visitVarInsn(FLOAD, i);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
        } else if (tp.equals(Type.DOUBLE_TYPE)) {
            mv.visitVarInsn(DLOAD, i);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
            return 1;
        } else {
            mv.visitVarInsn(ALOAD, i);
        }

        return 0;
    }

    private void start() {
        mv.visitCode();
    }

    private void end() {
        mv.visitMaxs(-1, -1);
        mv.visitEnd();
    }

    private void newEql() {
        mv.visitTypeInsn(NEW, eqlClassName);
        mv.visitInsn(DUP);
        MethodParam eqlConfig = methodAllParam.getEqlConfig();
        if (eqlConfig == null) {
            mv.visitLdcInsn(eqlerConfig != null ? eqlerConfig.value() : "DEFAULT");
            mv.visitMethodInsn(INVOKESPECIAL, eqlClassName, "<init>", "(Ljava/lang/String;)V", false);
        } else {
            mv.visitVarInsn(ALOAD, eqlConfig.getParamIndex() + 1);
            mv.visitMethodInsn(INVOKESPECIAL, eqlClassName, "<init>", "(Lorg/n3r/eql/config/EqlConfig;)V", false);
        }

        mv.visitMethodInsn(INVOKEVIRTUAL, eqlClassName, "me", "()Lorg/n3r/eql/Eql;", false);
    }

    private void result() {
        Class<?> returnType = method.getReturnType();
        Type tp = Type.getType(returnType);

        if (tp.equals(Type.BOOLEAN_TYPE)) {
            mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
            mv.visitInsn(IRETURN);
        } else if (tp.equals(Type.BYTE_TYPE)) {
            mv.visitTypeInsn(CHECKCAST, "java/lang/Byte");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false);
            mv.visitInsn(IRETURN);
        } else if (tp.equals(Type.CHAR_TYPE)) {
            mv.visitTypeInsn(CHECKCAST, "java/lang/Character");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false);
            mv.visitInsn(IRETURN);
        } else if (tp.equals(Type.SHORT_TYPE)) {
            mv.visitTypeInsn(CHECKCAST, "java/lang/Short");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false);
            mv.visitInsn(IRETURN);
        } else if (tp.equals(Type.INT_TYPE)) {
            mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
            mv.visitInsn(IRETURN);
        } else if (tp.equals(Type.LONG_TYPE)) {
            mv.visitTypeInsn(CHECKCAST, "java/lang/Long");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false);
            mv.visitInsn(LRETURN);
        } else if (tp.equals(Type.FLOAT_TYPE)) {
            mv.visitTypeInsn(CHECKCAST, "java/lang/Float");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
            mv.visitInsn(FRETURN);
        } else if (tp.equals(Type.DOUBLE_TYPE)) {
            mv.visitTypeInsn(CHECKCAST, "java/lang/Double");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
            mv.visitInsn(DRETURN);
        } else if (tp.equals(Type.VOID_TYPE)) {
            mv.visitInsn(POP);
            mv.visitInsn(RETURN);
        } else {
            mv.visitTypeInsn(CHECKCAST, Type.getInternalName(returnType));
            mv.visitInsn(ARETURN);
        }
    }

    private void execute() {
        Sql sqlAnn = method.getAnnotation(Sql.class);
        if (sqlAnn == null) {
            mv.visitInsn(ICONST_0);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/String");
        } else {
            String[] sqls = sqlAnn.value();
            mv.visitInsn(ICONST_0 + sqls.length);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/String");
            for (int i = 0; i < sqls.length; ++i) {
                mv.visitInsn(DUP);
                visitIntInsn(i);
                mv.visitLdcInsn(sqls[i]);
                mv.visitInsn(AASTORE);
            }
        }

        mv.visitMethodInsn(INVOKEVIRTUAL, EQL, "execute", "([Ljava/lang/String;)Ljava/lang/Object;", false);
    }

    private void limit() {
        Class<?> returnType = method.getReturnType();
        if (Collection.class.isAssignableFrom(returnType)) {
            MethodParam eqlPage = methodAllParam.getEqlPage();
            if (eqlPage != null) {
                mv.visitVarInsn(ALOAD, eqlPage.getParamIndex() + 1);
                mv.visitMethodInsn(INVOKEVIRTUAL, EQL, "limit",
                        "(Lorg/n3r/eql/EqlPage;)Lorg/n3r/eql/Eql;", false);
            }

        } else {
            mv.visitInsn(ICONST_1);
            mv.visitMethodInsn(INVOKEVIRTUAL, EQL, "limit", "(I)Lorg/n3r/eql/Eql;", false);
        }
    }

    private void returnType() {
        Class<?> returnTypeClass = method.getReturnType();
        Type tp = Type.getType(returnTypeClass);

        if (tp.equals(Type.BOOLEAN_TYPE)) {
            mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "TYPE", "Ljava/lang/Class;");
        } else if (tp.equals(Type.BYTE_TYPE)) {
            mv.visitFieldInsn(GETSTATIC, "java/lang/Byte", "TYPE", "Ljava/lang/Class;");
        } else if (tp.equals(Type.CHAR_TYPE)) {
            mv.visitFieldInsn(GETSTATIC, "java/lang/Character", "TYPE", "Ljava/lang/Class;");
        } else if (tp.equals(Type.SHORT_TYPE)) {
            mv.visitFieldInsn(GETSTATIC, "java/lang/Short", "TYPE", "Ljava/lang/Class;");
        } else if (tp.equals(Type.INT_TYPE)) {
            mv.visitFieldInsn(GETSTATIC, "java/lang/Integer", "TYPE", "Ljava/lang/Class;");
        } else if (tp.equals(Type.LONG_TYPE)) {
            mv.visitFieldInsn(GETSTATIC, "java/lang/Long", "TYPE", "Ljava/lang/Class;");
        } else if (tp.equals(Type.FLOAT_TYPE)) {
            mv.visitFieldInsn(GETSTATIC, "java/lang/Float", "TYPE", "Ljava/lang/Class;");
        } else if (tp.equals(Type.DOUBLE_TYPE)) {
            mv.visitFieldInsn(GETSTATIC, "java/lang/Double", "TYPE", "Ljava/lang/Class;");
        } else if (tp.equals(Type.VOID_TYPE)) {
            return;
        } else {
            java.lang.reflect.Type genericReturnType = method.getGenericReturnType();

            boolean isCollectionGeneric = genericReturnType instanceof ParameterizedType
                    && Collection.class.isAssignableFrom(returnTypeClass);
            if (isCollectionGeneric) {
                ParameterizedType parameterizedType = (ParameterizedType) genericReturnType;
                returnTypeClass = (Class) parameterizedType.getActualTypeArguments()[0];
            }

            Type returnType = Type.getType(returnTypeClass);
            mv.visitLdcInsn(returnType);
        }

        mv.visitMethodInsn(INVOKEVIRTUAL, EQL, "returnType",
                "(Ljava/lang/Class;)Lorg/n3r/eql/Eql;", false);
    }

    private void id() {
        Sql sqlAnn = method.getAnnotation(Sql.class);
        if (sqlAnn != null) {
            mv.visitLdcInsn(method.getName());
            mv.visitMethodInsn(INVOKEVIRTUAL, EQL, "tagSqlId", "(Ljava/lang/String;)Lorg/n3r/eql/Eql;", false);
            ;
        } else {
            SqlId sqlId = method.getAnnotation(SqlId.class);
            mv.visitLdcInsn(sqlId == null ? method.getName() : sqlId.value());
            mv.visitMethodInsn(INVOKEVIRTUAL, EQL, "id", "(Ljava/lang/String;)Lorg/n3r/eql/Eql;", false);
        }
    }

    private <T> void useSqlFile() {
        Sql sqlAnn = method.getAnnotation(Sql.class);
        if (sqlAnn != null) {
            mv.visitLdcInsn(Type.getType(eqlerClass));
            mv.visitMethodInsn(INVOKEVIRTUAL, EQL, "useSqlFile", "(Ljava/lang/Class;)Lorg/n3r/eql/Eql;", false);
            return;
        }

        UseSqlFile useSqlFile = method.getAnnotation(UseSqlFile.class);
        if (useSqlFile == null) useSqlFile = classUseSqlFile;
        if (useSqlFile != null) {
            if (S.isNotBlank(useSqlFile.value())) {
                mv.visitLdcInsn(useSqlFile.value());
                mv.visitMethodInsn(INVOKEVIRTUAL, EQL, "useSqlFile", "(Ljava/lang/String;)Lorg/n3r/eql/Eql;", false);
            } else if (useSqlFile.clazz() != Void.class) {
                mv.visitLdcInsn(Type.getType(useSqlFile.clazz()));
                mv.visitMethodInsn(INVOKEVIRTUAL, EQL, "useSqlFile", "(Ljava/lang/Class;)Lorg/n3r/eql/Eql;", false);
            } else {
                throw new RuntimeException("Bad @UseSqlFile usage!");
            }
        } else {
            mv.visitLdcInsn(Type.getType(eqlerClass));
            mv.visitMethodInsn(INVOKEVIRTUAL, EQL, "useSqlFile", "(Ljava/lang/Class;)Lorg/n3r/eql/Eql;", false);
        }
    }

    private void params() {
        if (methodAllParam.getNamedParamsCount() > 0) {
            visitIntInsn(1);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
            mv.visitInsn(DUP);
            mv.visitInsn(ICONST_0);
            mv.visitVarInsn(ALOAD,  methodAllParam.getAsmLocalVarNamedParamIndex());
            mv.visitInsn(AASTORE);
            mv.visitMethodInsn(INVOKEVIRTUAL, EQL, "params", "([Ljava/lang/Object;)Lorg/n3r/eql/Eql;", false);
            return;
        }

        if (methodAllParam.getSeqParamsCount() == 0) return;

        visitIntInsn(methodAllParam.getSeqParamsCount());

        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

        int index = 0;
        for (int i = 0, incrs = 0; i < methodAllParam.getMethodParamsCount(); ++i) {
            MethodParam methodParam = methodAllParam.getMethodParam(i);
            if (methodParam.getSeqParamIndex() < 0) continue;

            mv.visitInsn(DUP);
            visitIntInsn(index++);
            incrs += visitVar(i + 1 + incrs, Type.getType(methodParam.getParamType()));
            mv.visitInsn(AASTORE);
        }

        mv.visitMethodInsn(INVOKEVIRTUAL, EQL, "params", "([Ljava/lang/Object;)Lorg/n3r/eql/Eql;", false);
    }


    private void dynamics() {
        if (methodAllParam.getNamedDynamicCount() > 0) {
            visitIntInsn(1);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
            mv.visitInsn(DUP);
            mv.visitInsn(ICONST_0);
            mv.visitVarInsn(ALOAD,  methodAllParam.getAsmLocalVarNamedDynamicIndex());
            mv.visitInsn(AASTORE);
            mv.visitMethodInsn(INVOKEVIRTUAL, EQL, "dynamics", "([Ljava/lang/Object;)Lorg/n3r/eql/Eql;", false);
            return;
        }

        if (methodAllParam.getSeqDynamicsCount() == 0) return;

        visitIntInsn(methodAllParam.getSeqDynamicsCount());

        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

        int index = 0;
        for (int i = 0, incrs = 0; i < methodAllParam.getMethodParamsCount(); ++i) {
            MethodParam methodParam = methodAllParam.getMethodParam(i);
            if (methodParam.getSeqDynamicIndex() < 0) continue;

            mv.visitInsn(DUP);
            visitIntInsn(index++);
            incrs += visitVar(i + 1 + incrs, Type.getType(methodParam.getParamType()));
            mv.visitInsn(AASTORE);
        }

        mv.visitMethodInsn(INVOKEVIRTUAL, EQL, "dynamics", "([Ljava/lang/Object;)Lorg/n3r/eql/Eql;", false);

    }

    private MethodAllParam parseParams(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        Annotation[][] paramAnnotations = method.getParameterAnnotations();
        MethodAllParam methodAllParam = new MethodAllParam();

        for (int i = 0; i < parameterTypes.length; ++i) {
            MethodParam methodParam = new MethodParam();
            methodAllParam.addMethodParam(methodParam);

            methodParam.setParamIndex(i);
            methodParam.setParamType(parameterTypes[i]);
            methodParam.setParamAnnotations(paramAnnotations[i]);
        }

        methodAllParam.compute();

        return methodAllParam;
    }


    private void visitIntInsn(int i) {
        if (i <= 5) mv.visitInsn(ICONST_0 + i);
        else mv.visitIntInsn(BIPUSH, i);
    }
}
