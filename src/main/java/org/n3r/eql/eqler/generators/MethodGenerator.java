package org.n3r.eql.eqler.generators;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.n3r.eql.EqlPage;
import org.n3r.eql.EqlTran;
import org.n3r.eql.EqlTranable;
import org.n3r.eql.eqler.annotations.*;
import org.n3r.eql.impl.EqlBatch;
import org.n3r.eql.util.S;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class MethodGenerator<T> {
    private final Method method;
    private final MethodVisitor mv;
    private final Class<T> eqlerClass;
    private final EqlConfig eqlConfig;
    private final UseSqlFile classUseSqlFile;
    private final String eqlClassName;
    private List<MethodParam> methodParams;
    private boolean useNamedParams;
    private int varCount;
    private int normalParamCount;

    public MethodGenerator(ClassWriter classWriter, Method method, Class<T> eqlerClass) {
        this.method = method;
        this.eqlerClass = eqlerClass;
        EqlConfig eqlConfig = method.getAnnotation(EqlConfig.class);
        this.eqlConfig = eqlConfig != null ? eqlConfig : eqlerClass.getAnnotation(EqlConfig.class);
        this.eqlClassName = this.eqlConfig != null ? Type.getInternalName(this.eqlConfig.eql()) : "org/n3r/eql/Eql";
        this.classUseSqlFile = eqlerClass.getAnnotation(UseSqlFile.class);
        this.mv = classWriter.visitMethod(ACC_PUBLIC, method.getName(), Type.getMethodDescriptor(method), null, null);
        parseParams(method.getParameterTypes());
    }

    public void generate() {
        start();

        prepareNamedParams();
        newEql();
        useTran();
        useBatch();
        useSqlFile();
        params();
        id();
        returnType();
        limit();
        execute();
        result();

        end();
    }

    private void useBatch() {
        Optional<MethodParam> useBatchParam = findMethodParam(EqlBatch.class);
        if (!useBatchParam.isPresent()) return;

        mv.visitVarInsn(ALOAD, useBatchParam.get().getParamIndex() + 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/n3r/eql/Eql", "useBatch", "(Lorg/n3r/eql/impl/EqlBatch;)Lorg/n3r/eql/Eql;", false);
    }

    private Optional<MethodParam> findMethodParam(Class<?> paramType) {
        for (MethodParam methodParam : methodParams) {
            if (methodParam.getParamType() == paramType) {
                return Optional.of(methodParam);
            }
        }
        return Optional.absent();
    }

    private void useTran() {
        Optional<MethodParam> useTranParam = findMethodParam(EqlTran.class);
        if (useTranParam.isPresent()) {
            mv.visitVarInsn(ALOAD, useTranParam.get().getParamIndex() + 1);
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/n3r/eql/Eql", "useTran", "(Lorg/n3r/eql/EqlTran;)Lorg/n3r/eql/Eql;", false);
        } else if (EqlTranable.class.isAssignableFrom(eqlerClass)) {
            mv.visitMethodInsn(INVOKESTATIC, "org/n3r/eql/trans/EqlTranThreadLocal", "get", "()Lorg/n3r/eql/EqlTran;", false);
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/n3r/eql/Eql", "useTran", "(Lorg/n3r/eql/EqlTran;)Lorg/n3r/eql/Eql;", false);
        }
    }

    private void prepareNamedParams() {
        List<NamedParamDefine> namedParams = parseNameParams(method.getParameterTypes());
        if (namedParams.isEmpty()) return;

        this.useNamedParams = true;

        mv.visitMethodInsn(INVOKESTATIC, "com/google/common/collect/Maps", "newHashMap", "()Ljava/util/HashMap;", false);

        this.varCount = 1 /* this */ + 1 /* hashmap */ + method.getParameterTypes().length;
        mv.visitVarInsn(ASTORE, varCount);

        for (int i = 0, incrs = 0; i < namedParams.size(); ++i) {
            NamedParamDefine paramDefine = namedParams.get(i);
            mv.visitVarInsn(ALOAD, varCount);
            mv.visitLdcInsn(paramDefine.getParamName());

            incrs += visitVar(i + 1 + incrs, Type.getType(paramDefine.getParamType()));
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true);
            mv.visitInsn(POP);
        }
    }

    private List<NamedParamDefine> parseNameParams(Class<?>[] parameterTypes) {
        Annotation[][] parameterAnnsArr = method.getParameterAnnotations();
        List<NamedParamDefine> namedParams = Lists.newArrayList();
        for (int i = 0; i < parameterAnnsArr.length; ++i) {
            Annotation[] parameterAnns = parameterAnnsArr[i];
            PARAM_ANN:
            for (Annotation parameterAnn : parameterAnns) {
                if (parameterAnn instanceof NamedParam) {
                    namedParams.add(new NamedParamDefine(i, parameterTypes[i], (NamedParam) parameterAnn));
                    break PARAM_ANN;
                }
            }
        }
        return namedParams;
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
        mv.visitLdcInsn(eqlConfig != null ? eqlConfig.value() : "DEFAULT");
        mv.visitMethodInsn(INVOKESPECIAL, eqlClassName, "<init>", "(Ljava/lang/String;)V", false);
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

        mv.visitMethodInsn(INVOKEVIRTUAL, "org/n3r/eql/Eql", "execute", "([Ljava/lang/String;)Ljava/lang/Object;", false);
    }

    private void limit() {
        Class<?> returnType = method.getReturnType();
        if (Collection.class.isAssignableFrom(returnType)) {
            for (MethodParam methodParam : methodParams) {
                if (methodParam.getParamType() == EqlPage.class) {
                    mv.visitVarInsn(ALOAD, methodParam.getParamIndex() + 1);
                    mv.visitMethodInsn(INVOKEVIRTUAL, "org/n3r/eql/Eql", "limit",
                            "(Lorg/n3r/eql/EqlPage;)Lorg/n3r/eql/Eql;", false);
                    break;
                }
            }

        } else {
            mv.visitInsn(ICONST_1);
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/n3r/eql/Eql", "limit", "(I)Lorg/n3r/eql/Eql;", false);
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

            if (genericReturnType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericReturnType;
                returnTypeClass = (Class) parameterizedType.getActualTypeArguments()[0];
            } else {
                returnTypeClass = (Class) genericReturnType;
            }

            Type returnType = Type.getType(returnTypeClass);
            mv.visitLdcInsn(returnType);
        }

        mv.visitMethodInsn(INVOKEVIRTUAL, "org/n3r/eql/Eql", "returnType",
                "(Ljava/lang/Class;)Lorg/n3r/eql/Eql;", false);
    }

    private void id() {
        Sql sqlAnn = method.getAnnotation(Sql.class);
        if (sqlAnn != null) return;

        SqlId sqlId = method.getAnnotation(SqlId.class);
        mv.visitLdcInsn(sqlId == null ? method.getName() : sqlId.value());
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/n3r/eql/Eql", "id", "(Ljava/lang/String;)Lorg/n3r/eql/Eql;", false);
    }

    private <T> void useSqlFile() {
        Sql sqlAnn = method.getAnnotation(Sql.class);
        if (sqlAnn != null) return;

        UseSqlFile useSqlFile = method.getAnnotation(UseSqlFile.class);
        if (useSqlFile == null) useSqlFile = classUseSqlFile;
        if (useSqlFile != null) {
            if (S.isNotBlank(useSqlFile.value())) {
                mv.visitLdcInsn(useSqlFile.value());
                mv.visitMethodInsn(INVOKEVIRTUAL, "org/n3r/eql/Eql", "useSqlFile", "(Ljava/lang/String;)Lorg/n3r/eql/Eql;", false);
            } else if (useSqlFile.clazz() != Void.class) {
                mv.visitLdcInsn(Type.getType(useSqlFile.clazz()));
                mv.visitMethodInsn(INVOKEVIRTUAL, "org/n3r/eql/Eql", "useSqlFile", "(Ljava/lang/Class;)Lorg/n3r/eql/Eql;", false);
            } else {
                throw new RuntimeException("Bad @UseSqlFile usage!");
            }
        } else {
            mv.visitLdcInsn(Type.getType(eqlerClass));
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/n3r/eql/Eql", "useSqlFile", "(Ljava/lang/Class;)Lorg/n3r/eql/Eql;", false);
        }
    }

    private void params() {
        if (useNamedParams) {
            visitIntInsn(1);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
            mv.visitInsn(DUP);
            mv.visitInsn(ICONST_0);
            mv.visitVarInsn(ALOAD, varCount);
            mv.visitInsn(AASTORE);
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/n3r/eql/Eql", "params", "([Ljava/lang/Object;)Lorg/n3r/eql/Eql;", false);
            return;
        }

        if (normalParamCount == 0) return;

        visitIntInsn(normalParamCount);

        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

        int index = 0;
        for (int i = 0, incrs = 0; i < method.getParameterTypes().length; ++i) {
            MethodParam methodParam = methodParams.get(i);
            if (!methodParam.isNormal()) continue;

            mv.visitInsn(DUP);
            visitIntInsn(index++);
            incrs += visitVar(i + 1 + incrs, Type.getType(methodParam.getParamType()));
            mv.visitInsn(AASTORE);
        }

        mv.visitMethodInsn(INVOKEVIRTUAL, "org/n3r/eql/Eql", "params", "([Ljava/lang/Object;)Lorg/n3r/eql/Eql;", false);
    }

    private void parseParams(Class<?>[] parameterTypes) {
        this.methodParams = Lists.newArrayList();
        for (int i = 0; i < parameterTypes.length; ++i) {
            Class<?> pt = parameterTypes[i];
            if (EqlPage.class == pt || EqlTran.class == pt || EqlBatch.class == pt) {
                methodParams.add(new MethodParam(i, pt, false));
            } else {
                methodParams.add(new MethodParam(i, pt, true));
                ++normalParamCount;
            }
        }
    }

    private void visitIntInsn(int i) {
        if (i <= 5) mv.visitInsn(ICONST_0 + i);
        else mv.visitIntInsn(BIPUSH, i);
    }
}
