package org.n3r.eql.eqler.generators;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.val;
import org.n3r.eql.Eql;
import org.n3r.eql.EqlPage;
import org.n3r.eql.EqlTran;
import org.n3r.eql.EqlTranable;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.eqler.OnErr;
import org.n3r.eql.eqler.annotations.*;
import org.n3r.eql.impl.EqlBatch;
import org.n3r.eql.map.EqlRowMapper;
import org.n3r.eql.pojo.annotations.EqlId;
import org.n3r.eql.trans.EqlTranThreadLocal;
import org.n3r.eql.util.S;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.n3r.eql.util.Asms.p;
import static org.n3r.eql.util.Asms.sig;
import static org.objectweb.asm.Opcodes.*;

public class MethodGenerator<T> implements Generatable {
    public static final String EQL = p(Eql.class);
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
        val methodEqlerConfig = method.getAnnotation(EqlerConfig.class);
        val classEqlerConfig = eqlerClass.getAnnotation(EqlerConfig.class);
        this.eqlerConfig = methodEqlerConfig != null ? methodEqlerConfig : classEqlerConfig;
        this.eqlClassName = this.eqlerConfig != null ? Type.getInternalName(this.eqlerConfig.eql()) : EQL;
        this.classUseSqlFile = eqlerClass.getAnnotation(UseSqlFile.class);
        this.mv = classWriter.visitMethod(ACC_PUBLIC, method.getName(),
                Type.getMethodDescriptor(method), null, null);
        this.methodAllParam = parseParams(method);
    }

    public void generate() {
        start();

        prepareNamedParams();
        prepareNamedDynamics();

        val sqls = parseSqls(method);

        newEql();
        useTran();
        useBatch();
        useSqlFile();
        params();
        dynamics();
        id(sqls);
        returnType();
        limit();
        options();
        execute(sqls);
        result();

        end();
    }

    private void useBatch() {
        val eqlBatch = methodAllParam.getEqlBatch();
        if (eqlBatch == null) return;

        mv.visitVarInsn(ALOAD, eqlBatch.getParamIndex() + 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, EQL, "useBatch", sig(Eql.class, EqlBatch.class), false);
    }

    private void useTran() {
        val eqlTran = methodAllParam.getEqlTran();
        if (eqlTran != null) {
            mv.visitVarInsn(ALOAD, eqlTran.getParamIndex() + 1);
            mv.visitMethodInsn(INVOKEVIRTUAL, EQL, "useTran", sig(Eql.class, EqlTran.class), false);
        } else if (EqlTranable.class.isAssignableFrom(eqlerClass)) {
            mv.visitMethodInsn(INVOKESTATIC, p(EqlTranThreadLocal.class), "get", sig(EqlTran.class), false);
            mv.visitMethodInsn(INVOKEVIRTUAL, EQL, "useTran", sig(Eql.class, EqlTran.class), false);
        }
    }

    private void prepareNamedParams() {
        if (methodAllParam.getNamedParamsCount() == 0) return;

        mv.visitMethodInsn(INVOKESTATIC, p(Maps.class), "newHashMap", sig(HashMap.class), false);

        mv.visitVarInsn(ASTORE, methodAllParam.getAsmLocalVarNamedParamIndex());

        for (int i = 0; i < methodAllParam.getMethodParamsCount(); ++i) {
            val methodParam = methodAllParam.getMethodParam(i);
            val param = methodParam.getParam();
            if (param == null) continue;

            mv.visitVarInsn(ALOAD, methodAllParam.getAsmLocalVarNamedParamIndex());
            mv.visitLdcInsn(param.value());

            visitVar(i + 1 + methodParam.getOffset(), Type.getType(methodParam.getParamType()));
            mv.visitMethodInsn(INVOKEINTERFACE, p(Map.class), "put",
                    sig(Object.class, Object.class, Object.class), true);
            mv.visitInsn(POP);
        }
    }

    private void prepareNamedDynamics() {
        if (methodAllParam.getNamedDynamicCount() == 0) return;

        mv.visitMethodInsn(INVOKESTATIC, p(Maps.class), "newHashMap", sig(HashMap.class), false);

        mv.visitVarInsn(ASTORE, methodAllParam.getAsmLocalVarNamedDynamicIndex());

        for (int i = 0; i < methodAllParam.getMethodParamsCount(); ++i) {
            val methodParam = methodAllParam.getMethodParam(i);
            val dynamic = methodParam.getDynamic();
            if (dynamic == null) continue;
            if (isBlank(dynamic.value()) && isBlank(dynamic.name())) continue;

            mv.visitVarInsn(ALOAD, methodAllParam.getAsmLocalVarNamedDynamicIndex());
            mv.visitLdcInsn(isBlank(dynamic.value()) ? dynamic.name() : dynamic.value());

            visitVar(i + 1 + methodParam.getOffset(), Type.getType(methodParam.getParamType()));
            mv.visitMethodInsn(INVOKEINTERFACE, p(Map.class), "put",
                    sig(Object.class, Object.class, Object.class), true);
            mv.visitInsn(POP);
        }
    }

    private void visitVar(int i, Type tp) {
        if (tp.equals(Type.BOOLEAN_TYPE)) {
            mv.visitVarInsn(ILOAD, i);
            mv.visitMethodInsn(INVOKESTATIC, p(Boolean.class), "valueOf", "(Z)Ljava/lang/Boolean;", false);
        } else if (tp.equals(Type.BYTE_TYPE)) {
            mv.visitVarInsn(ILOAD, i);
            mv.visitMethodInsn(INVOKESTATIC, p(Byte.class), "valueOf", "(B)Ljava/lang/Byte;", false);
        } else if (tp.equals(Type.CHAR_TYPE)) {
            mv.visitVarInsn(ILOAD, i);
            mv.visitMethodInsn(INVOKESTATIC, p(Character.class), "valueOf", "(C)Ljava/lang/Character;", false);
        } else if (tp.equals(Type.SHORT_TYPE)) {
            mv.visitVarInsn(ILOAD, i);
            mv.visitMethodInsn(INVOKESTATIC, p(Short.class), "valueOf", "(S)Ljava/lang/Short;", false);
        } else if (tp.equals(Type.INT_TYPE)) {
            mv.visitVarInsn(ILOAD, i);
            mv.visitMethodInsn(INVOKESTATIC, p(Integer.class), "valueOf", "(I)Ljava/lang/Integer;", false);
        } else if (tp.equals(Type.LONG_TYPE)) {
            mv.visitVarInsn(LLOAD, i);
            mv.visitMethodInsn(INVOKESTATIC, p(Long.class), "valueOf", "(J)Ljava/lang/Long;", false);
        } else if (tp.equals(Type.FLOAT_TYPE)) {
            mv.visitVarInsn(FLOAD, i);
            mv.visitMethodInsn(INVOKESTATIC, p(Float.class), "valueOf", "(F)Ljava/lang/Float;", false);
        } else if (tp.equals(Type.DOUBLE_TYPE)) {
            mv.visitVarInsn(DLOAD, i);
            mv.visitMethodInsn(INVOKESTATIC, p(Double.class), "valueOf", "(D)Ljava/lang/Double;", false);
        } else {
            mv.visitVarInsn(ALOAD, i);
        }

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
        val eqlConfig = methodAllParam.getEqlConfig();
        if (eqlConfig == null) {
            mv.visitLdcInsn(eqlerConfig != null ? eqlerConfig.value() : "DEFAULT");
            mv.visitMethodInsn(INVOKESPECIAL, eqlClassName, "<init>",
                    sig(void.class, String.class), false);
        } else {
            mv.visitVarInsn(ALOAD, eqlConfig.getParamIndex() + 1);
            mv.visitMethodInsn(INVOKESPECIAL, eqlClassName, "<init>",
                    sig(void.class, EqlConfig.class), false);
        }

        mv.visitMethodInsn(INVOKEVIRTUAL, eqlClassName, "me",
                sig(Eql.class), false);
    }

    private void result() {
        Class<?> returnType = method.getReturnType();
        val tp = Type.getType(returnType);

        if (tp.equals(Type.BOOLEAN_TYPE)) {
            mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
            mv.visitInsn(IRETURN);
        } else if (tp.equals(Type.BYTE_TYPE)) {
            mv.visitTypeInsn(CHECKCAST, "java/lang/Byte");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B", false);
            mv.visitInsn(IRETURN);
        } else if (tp.equals(Type.CHAR_TYPE)) {
            mv.visitTypeInsn(CHECKCAST, "java/lang/Character");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C", false);
            mv.visitInsn(IRETURN);
        } else if (tp.equals(Type.SHORT_TYPE)) {
            mv.visitTypeInsn(CHECKCAST, "java/lang/Short");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S", false);
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
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F", false);
            mv.visitInsn(FRETURN);
        } else if (tp.equals(Type.DOUBLE_TYPE)) {
            mv.visitTypeInsn(CHECKCAST, "java/lang/Double");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D", false);
            mv.visitInsn(DRETURN);
        } else if (tp.equals(Type.VOID_TYPE)) {
            mv.visitInsn(POP);
            mv.visitInsn(RETURN);
        } else {
            mv.visitTypeInsn(CHECKCAST, Type.getInternalName(returnType));
            mv.visitInsn(ARETURN);
        }
    }

    private void options() {
        val sqlOptions = method.getAnnotation(SqlOptions.class);
        if (sqlOptions == null) return;

        mv.visitLdcInsn(createOptions(sqlOptions));
        mv.visitMethodInsn(INVOKEVIRTUAL, EQL, "options",
                sig(Eql.class, String.class), false);
    }

    private String createOptions(SqlOptions sqlOptions) {
        val optionsStr = new StringBuilder();
        if (sqlOptions.iterate()) optionsStr.append(" iterate ");
        if (sqlOptions.onErr() == OnErr.Resume)
            optionsStr.append(" onerr=resume ");
        String split = sqlOptions.split();
        if (S.isNotEmpty(split)) optionsStr.append(" split=").append(split);

        optionsStr.append(' ').append(sqlOptions.value());

        return optionsStr.toString();
    }

    private void execute(String[] sqls) {
        if (sqls.length == 0) {
            mv.visitInsn(ICONST_0);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/String");
        } else {
            mv.visitInsn(ICONST_0 + sqls.length);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/String");
            for (int i = 0; i < sqls.length; ++i) {
                mv.visitInsn(DUP);
                visitIntInsn(i);
                mv.visitLdcInsn(sqls[i]);
                mv.visitInsn(AASTORE);
            }
        }

        mv.visitMethodInsn(INVOKEVIRTUAL, EQL, "execute",
                sig(Object.class, String[].class), false);
    }

    private String[] parseSqls(Method method) {
        List<String> sqls = Lists.newArrayList();
        val activeProfiles = parseActiveProfiles();

        for (val annotation : method.getAnnotations()) {
            if (annotation instanceof Sql) {
                for (val sql : ((Sql) annotation).value()) {
                    sqls.add(sql);
                }
            } else if (annotation instanceof ProfiledSql) {
                addProfiledSqls(activeProfiles, sqls, (ProfiledSql) annotation);
            } else if (annotation instanceof ProfiledSqls) {
                for (val profileSql : ((ProfiledSqls) annotation).value()) {
                    addProfiledSqls(activeProfiles, sqls, profileSql);
                }
            }
        }

        return sqls.toArray(new String[sqls.size()]);
    }

    private Set<String> parseActiveProfiles() {
        val activeProfiles = ActiveProfilesThreadLocal.get();
        if (activeProfiles == null) {
            return Sets.newHashSet();
        }

        return Sets.newHashSet(activeProfiles);
    }

    private void addProfiledSqls(Set<String> activeProfiles,
                                 List<String> sqls, ProfiledSql profiledSql) {
        if (containsInActiveProfiles(activeProfiles, profiledSql.profile())) {
            for (val sql : profiledSql.sql()) {
                sqls.add(sql);
            }
        }
    }

    private boolean containsInActiveProfiles(Set<String> activeProfiles, String[] profile) {
        if (profile.length == 0) {
            return true;
        }

        for (val profileItem : profile) {
            if (activeProfiles.contains(profileItem)) return true;
        }

        return false;
    }

    private void limit() {
        Class<?> returnType = method.getReturnType();
        if (Collection.class.isAssignableFrom(returnType)) {
            val eqlPage = methodAllParam.getEqlPage();
            if (eqlPage != null) {
                mv.visitVarInsn(ALOAD, eqlPage.getParamIndex() + 1);
                mv.visitMethodInsn(INVOKEVIRTUAL, EQL, "limit",
                        sig(Eql.class, EqlPage.class), false);
            }

        } else {
            MethodParam eqlRowMapper = methodAllParam.getEqlRowMapper();
            EqlMapper eqlMapper = method.getAnnotation(EqlMapper.class);
            if (eqlRowMapper == null && eqlMapper == null) {
                mv.visitInsn(ICONST_1);
                mv.visitMethodInsn(INVOKEVIRTUAL, EQL, "limit",
                        sig(Eql.class, int.class), false);
            }
        }
    }

    private void returnType() {
        Class<?> returnTypeClass = method.getReturnType();
        Type tp = Type.getType(returnTypeClass);

        val eqlRowMapper = methodAllParam.getEqlRowMapper();
        if (eqlRowMapper != null) {
            mv.visitVarInsn(ALOAD, eqlRowMapper.getVarIndex());
            mv.visitMethodInsn(INVOKEVIRTUAL, EQL, "returnType",
                    sig(Eql.class, EqlRowMapper.class), false);
            return;
        }

        val eqlMapper = method.getAnnotation(EqlMapper.class);
        if (eqlMapper != null) {
            Type returnType = Type.getType(eqlMapper.value());
            mv.visitLdcInsn(returnType);
            mv.visitMethodInsn(INVOKEVIRTUAL, EQL, "returnType",
                    sig(Eql.class, Class.class), false);
            return;
        }

        val paramReturnType = methodAllParam.getParamReturnType();
        if (paramReturnType != null) {
            mv.visitVarInsn(ALOAD, paramReturnType.getVarIndex());
            mv.visitMethodInsn(INVOKEVIRTUAL, EQL, "returnType",
                    sig(Eql.class, Class.class), false);
            return;
        }

        if (tp.equals(Type.VOID_TYPE)) return;

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
        } else {
            val genericReturnType = method.getGenericReturnType();

            val isCollectionGeneric = genericReturnType instanceof ParameterizedType
                    && Collection.class.isAssignableFrom(returnTypeClass);
            if (isCollectionGeneric) {
                val parameterizedType = (ParameterizedType) genericReturnType;
                returnTypeClass = (Class) parameterizedType.getActualTypeArguments()[0];
            }

            val returnType = Type.getType(returnTypeClass);
            mv.visitLdcInsn(returnType);
        }

        mv.visitMethodInsn(INVOKEVIRTUAL, EQL, "returnType",
                sig(Eql.class, Class.class), false);
    }

    private void id(String[] sqls) {
        if (sqls.length > 0) {
            mv.visitLdcInsn(method.getName());
            mv.visitMethodInsn(INVOKEVIRTUAL, EQL, "tagSqlId",
                    sig(Eql.class, String.class), false);
        } else {
            val sqlId = method.getAnnotation(SqlId.class);
            val paramEqlId = methodAllParam.getParamEqlId();
            if (paramEqlId == null) {
                mv.visitLdcInsn(sqlId == null ? method.getName() : sqlId.value());
            } else {
                mv.visitVarInsn(ALOAD, paramEqlId.getParamIndex() + 1);
            }
            mv.visitMethodInsn(INVOKEVIRTUAL, EQL, "id",
                    sig(Eql.class, String.class), false);
        }
    }

    private <T> void useSqlFile() {
        Sql sqlAnn = method.getAnnotation(Sql.class);
        if (sqlAnn != null) return;

        UseSqlFile useSqlFile = method.getAnnotation(UseSqlFile.class);
        if (useSqlFile == null) useSqlFile = classUseSqlFile;
        if (useSqlFile != null) {
            if (S.isNotBlank(useSqlFile.value())) {
                mv.visitLdcInsn(useSqlFile.value());
                mv.visitMethodInsn(INVOKEVIRTUAL, EQL, "useSqlFile",
                        sig(Eql.class, String.class), false);
            } else if (useSqlFile.clazz() != Void.class) {
                mv.visitLdcInsn(Type.getType(useSqlFile.clazz()));
                mv.visitMethodInsn(INVOKEVIRTUAL, EQL, "useSqlFile",
                        sig(Eql.class, Class.class), false);
            } else {
                throw new RuntimeException("Bad @UseSqlFile usage!");
            }
        } else {
            mv.visitLdcInsn(Type.getType(eqlerClass));
            mv.visitMethodInsn(INVOKEVIRTUAL, EQL, "useSqlFile",
                    sig(Eql.class, Class.class), false);
        }
    }

    private void params() {
        if (methodAllParam.getNamedParamsCount() > 0) {
            visitIntInsn(1);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
            mv.visitInsn(DUP);
            mv.visitInsn(ICONST_0);
            mv.visitVarInsn(ALOAD, methodAllParam.getAsmLocalVarNamedParamIndex());
            mv.visitInsn(AASTORE);
            mv.visitMethodInsn(INVOKEVIRTUAL, EQL, "params",
                    sig(Eql.class, Object[].class), false);
            return;
        }

        if (methodAllParam.getSeqParamsCount() == 0) return;

        visitIntInsn(methodAllParam.getSeqParamsCount());

        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

        int index = 0;
        for (int i = 0; i < methodAllParam.getParamsSize(); ++i) {
            val methodParam = methodAllParam.getMethodParam(i);
            if (methodParam.getSeqParamIndex() < 0) continue;

            mv.visitInsn(DUP);
            visitIntInsn(index++);
            visitVar(i + 1 + methodParam.getOffset(), Type.getType(methodParam.getParamType()));
            mv.visitInsn(AASTORE);
        }

        mv.visitMethodInsn(INVOKEVIRTUAL, EQL, "params",
                sig(Eql.class, Object[].class), false);
    }


    private void dynamics() {
        if (methodAllParam.getNamedDynamicCount() > 0) {
            visitIntInsn(1);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
            mv.visitInsn(DUP);
            mv.visitInsn(ICONST_0);
            mv.visitVarInsn(ALOAD, methodAllParam.getAsmLocalVarNamedDynamicIndex());
            mv.visitInsn(AASTORE);
            mv.visitMethodInsn(INVOKEVIRTUAL, EQL, "dynamics",
                    sig(Eql.class, Object[].class), false);
            return;
        }

        if (methodAllParam.getSeqDynamicsCount() == 0) return;

        visitIntInsn(methodAllParam.getSeqDynamicsCount());

        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

        int index = 0;
        for (int i = 0; i < methodAllParam.getParamsSize(); ++i) {
            val methodParam = methodAllParam.getMethodParam(i);
            if (methodParam.getSeqDynamicIndex() < 0) continue;

            mv.visitInsn(DUP);
            visitIntInsn(index++);
            visitVar(i + 1 + methodParam.getOffset(), Type.getType(methodParam.getParamType()));
            mv.visitInsn(AASTORE);
        }

        mv.visitMethodInsn(INVOKEVIRTUAL, EQL, "dynamics",
                sig(Eql.class, Object[].class), false);

    }

    private MethodAllParam parseParams(Method method) {
        val parameterTypes = method.getParameterTypes();
        val paramAnnotations = method.getParameterAnnotations();
        val methodAllParam = new MethodAllParam();

        for (int i = 0; i < parameterTypes.length; ++i) {
            val methodParam = new MethodParam();
            methodAllParam.addMethodParam(methodParam);

            methodParam.setParamIndex(i);
            methodParam.setParamType(parameterTypes[i]);
            methodParam.setParamAnnotations(paramAnnotations[i]);
        }

        methodAllParam.setMethodEqlId(method.getAnnotation(EqlId.class));
        methodAllParam.compute();

        return methodAllParam;
    }


    private void visitIntInsn(int i) {
        if (i <= 5) mv.visitInsn(ICONST_0 + i);
        else mv.visitIntInsn(BIPUSH, i);
    }
}
