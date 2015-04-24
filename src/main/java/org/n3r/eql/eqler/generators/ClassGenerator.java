package org.n3r.eql.eqler.generators;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.Method;

import static org.objectweb.asm.Opcodes.*;

public class ClassGenerator<T> {
    private final Class<T> eqlerClass;
    private final String implName;

    public ClassGenerator(Class<T> eqlerClass) {
        this.eqlerClass = eqlerClass;
        this.implName = eqlerClass.getName() + "Impl";
    }


    public Class<? extends T> generate() {
        byte[] bytes = createEqlImplClassBytes();
        return defineClass(bytes);
    }

    private Class<? extends T> defineClass(byte[] bytes) {
        return (Class<? extends T>) new OwnClassLoader().defineClass(implName, bytes);
    }


    private byte[] createEqlImplClassBytes() {
        ClassWriter cw = createClass(implName);

        constructor(cw);

        for (Method method : eqlerClass.getDeclaredMethods()) {
            new MethodGenerator(cw, method, eqlerClass).generate();
        }

        return createBytes(cw);
    }

    private byte[] createBytes(ClassWriter cw) {
        cw.visitEnd();
        return cw.toByteArray();
    }

    private class OwnClassLoader extends ClassLoader {
        public Class<?> defineClass(String name, byte[] b) {
            return defineClass(name, b, 0, b.length);
        }
    }

    private ClassWriter createClass(String implName) {
        final String implSourceName = implName.replace('.', '/');
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, implSourceName, null, "java/lang/Object",
                new String[]{"org/n3r/eql/eqler/MyEqler"});
        return cw;
    }

    private void constructor(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

}
