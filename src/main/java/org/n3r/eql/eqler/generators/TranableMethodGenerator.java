package org.n3r.eql.eqler.generators;

import org.n3r.eql.Eql;
import org.n3r.eql.EqlTran;
import org.n3r.eql.eqler.annotations.EqlerConfig;
import org.n3r.eql.trans.EqlTranThreadLocal;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;

import static org.n3r.eql.util.Asms.p;
import static org.objectweb.asm.Opcodes.*;

public class TranableMethodGenerator<T>  implements Generatable{
    private final String methodName;
    private final String eqlClassName;
    private final EqlerConfig eqlerConfig;
    private final ClassWriter cw;

    public TranableMethodGenerator(ClassWriter classWriter, Method method, Class<T> eqlerClass) {
        this.methodName = method.getName();
        this.cw = classWriter;

        EqlerConfig eqlerConfig = eqlerClass.getAnnotation(EqlerConfig.class);
        this.eqlerConfig = eqlerConfig != null ?
                eqlerConfig : eqlerClass.getAnnotation(EqlerConfig.class);
        this.eqlClassName = eqlerConfig != null ?
                Type.getInternalName(eqlerConfig.eql()) : p(Eql.class);
    }

    public void generate() {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, methodName, "()V", null, null);
        mv.visitCode();

        if ("start".equals(methodName)) {
            start(mv);
        } else if ("commit".equals(methodName) || "rollback".equals(methodName)) {
            commitOrRollback(mv, methodName);
        } else if ("close".equals(methodName)) {
            close(mv);
        }

        mv.visitInsn(RETURN);
        mv.visitMaxs(-1, -1);
        mv.visitEnd();
    }

    private void close(MethodVisitor mv) {
        mv.visitMethodInsn(INVOKESTATIC, p(EqlTranThreadLocal.class), "get",
                "()Lorg/n3r/eql/EqlTran;", false);
        mv.visitVarInsn(ASTORE, 1);
        mv.visitVarInsn(ALOAD, 1);
        Label l0 = new Label();
        mv.visitJumpInsn(IFNULL, l0);
        mv.visitMethodInsn(INVOKESTATIC, p(EqlTranThreadLocal.class), "clear", "()V", false);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEINTERFACE, p(EqlTran.class), "close", "()V", true);
        mv.visitLabel(l0);
        mv.visitFrame(F_APPEND, 1, new Object[]{p(EqlTran.class)}, 0, null);
    }

    private void commitOrRollback(MethodVisitor mv, String methodName) {
        mv.visitMethodInsn(INVOKESTATIC, p(EqlTranThreadLocal.class), "get",
                "()Lorg/n3r/eql/EqlTran;", false);
        mv.visitVarInsn(ASTORE, 1);
        mv.visitVarInsn(ALOAD, 1);
        Label l0 = new Label();
        mv.visitJumpInsn(IFNULL, l0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEINTERFACE, p(EqlTran.class), methodName, "()V", true);
        mv.visitLabel(l0);
        mv.visitFrame(F_APPEND, 1, new Object[]{p(EqlTran.class)}, 0, null);
    }

    private void newEql(MethodVisitor mv) {
        mv.visitTypeInsn(NEW, eqlClassName);
        mv.visitInsn(DUP);
        mv.visitLdcInsn(eqlerConfig != null ? eqlerConfig.value() : "DEFAULT");
        mv.visitMethodInsn(INVOKESPECIAL, eqlClassName, "<init>",
                "(Ljava/lang/String;)V", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, eqlClassName, "me",
                "()Lorg/n3r/eql/Eql;", false);
    }

    private void start(MethodVisitor mv) {
        mv.visitMethodInsn(INVOKESTATIC, p(EqlTranThreadLocal.class), "get",
                "()Lorg/n3r/eql/EqlTran;", false);
        mv.visitVarInsn(ASTORE, 1);
        mv.visitVarInsn(ALOAD, 1);
        Label l0 = new Label();
        mv.visitJumpInsn(IFNULL, l0);
        mv.visitInsn(RETURN);
        mv.visitLabel(l0);
        mv.visitFrame(F_APPEND, 1, new Object[]{p(EqlTran.class)}, 0, null);
        newEql(mv);
        mv.visitMethodInsn(INVOKEVIRTUAL, p(Eql.class), "newTran",
                "()Lorg/n3r/eql/EqlTran;", false);
        mv.visitVarInsn(ASTORE, 1);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKESTATIC, p(EqlTranThreadLocal.class), "set",
                "(Lorg/n3r/eql/EqlTran;)V", false);
    }

    public static boolean isEqlTranableMethod(Method method) {
        if ("()V".equals(Type.getMethodDescriptor(method))) {
            String name = method.getName();
            return "start".equals(name) ||
                    "commit".equals(name) ||
                    "rollback".equals(name) ||
                    "close".equals(name);
        }

        return false;
    }
}
