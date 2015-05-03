package org.n3r.eql.eqler.generators;

import org.n3r.eql.eqler.annotations.EqlerConfig;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;

import static org.objectweb.asm.Opcodes.*;

public class TranableMethodGenerator<T> {
    private final String methodName;
    private final String eqlClassName;
    private final EqlerConfig eqlerConfig;
    private final ClassWriter cw;

    public TranableMethodGenerator(ClassWriter classWriter, Method method, Class<T> eqlerClass) {
        this.methodName = method.getName();
        this.cw = classWriter;

        EqlerConfig eqlerConfig = method.getAnnotation(EqlerConfig.class);
        this.eqlerConfig = eqlerConfig != null ? eqlerConfig : eqlerClass.getAnnotation(EqlerConfig.class);
        this.eqlClassName = eqlerConfig != null ? Type.getInternalName(eqlerConfig.eql()) : "org/n3r/eql/Eql";
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
        mv.visitMethodInsn(INVOKESTATIC, "org/n3r/eql/trans/EqlTranThreadLocal", "get", "()Lorg/n3r/eql/EqlTran;", false);
        mv.visitVarInsn(ASTORE, 1);
        mv.visitVarInsn(ALOAD, 1);
        Label l0 = new Label();
        mv.visitJumpInsn(IFNULL, l0);
        mv.visitMethodInsn(INVOKESTATIC, "org/n3r/eql/trans/EqlTranThreadLocal", "clear", "()V", false);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEINTERFACE, "org/n3r/eql/EqlTran", "close", "()V", true);
        mv.visitLabel(l0);
        mv.visitFrame(F_APPEND, 1, new Object[]{"org/n3r/eql/EqlTran"}, 0, null);
    }

    private void commitOrRollback(MethodVisitor mv, String methodName) {
        mv.visitMethodInsn(INVOKESTATIC, "org/n3r/eql/trans/EqlTranThreadLocal", "get", "()Lorg/n3r/eql/EqlTran;", false);
        mv.visitVarInsn(ASTORE, 1);
        mv.visitVarInsn(ALOAD, 1);
        Label l0 = new Label();
        mv.visitJumpInsn(IFNULL, l0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEINTERFACE, "org/n3r/eql/EqlTran", methodName, "()V", true);
        mv.visitLabel(l0);
        mv.visitFrame(F_APPEND, 1, new Object[]{"org/n3r/eql/EqlTran"}, 0, null);
    }

    private void newEql(MethodVisitor mv) {
        mv.visitTypeInsn(NEW, eqlClassName);
        mv.visitInsn(DUP);
        mv.visitLdcInsn(eqlerConfig != null ? eqlerConfig.value() : "DEFAULT");
        mv.visitMethodInsn(INVOKESPECIAL, eqlClassName, "<init>", "(Ljava/lang/String;)V", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, eqlClassName, "me", "()Lorg/n3r/eql/Eql;", false);
    }

    private void start(MethodVisitor mv) {
        mv.visitMethodInsn(INVOKESTATIC, "org/n3r/eql/trans/EqlTranThreadLocal", "get", "()Lorg/n3r/eql/EqlTran;", false);
        mv.visitVarInsn(ASTORE, 1);
        mv.visitVarInsn(ALOAD, 1);
        Label l0 = new Label();
        mv.visitJumpInsn(IFNULL, l0);
        mv.visitInsn(RETURN);
        mv.visitLabel(l0);
        mv.visitFrame(F_APPEND, 1, new Object[]{"org/n3r/eql/EqlTran"}, 0, null);
        newEql(mv);
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/n3r/eql/Eql", "newTran", "()Lorg/n3r/eql/EqlTran;", false);
        mv.visitVarInsn(ASTORE, 1);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKESTATIC, "org/n3r/eql/trans/EqlTranThreadLocal", "set", "(Lorg/n3r/eql/EqlTran;)V", false);
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
