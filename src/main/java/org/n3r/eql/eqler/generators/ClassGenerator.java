package org.n3r.eql.eqler.generators;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;

import static org.objectweb.asm.Opcodes.*;

public class ClassGenerator<T> {
    private final Class<T> eqlerClass;
    private final String implName;
    private final ClassWriter classWriter;

    public ClassGenerator(Class<T> eqlerClass) {
        this.eqlerClass = eqlerClass;
        this.implName = eqlerClass.getName() + "Impl";
        this.classWriter = createClassWriter();
    }


    public Class<? extends T> generate() {
        byte[] bytes = createEqlImplClassBytes();
        return defineClass(bytes);
    }

    private Class<? extends T> defineClass(byte[] bytes) {
        return (Class<? extends T>) new EqlerClassLoader().defineClass(implName, bytes);
    }

    private byte[] createEqlImplClassBytes() {
        constructor();

        for (Method method : eqlerClass.getMethods()) {
            if (TranableMethodGenerator.isEqlTranableMethod(method)) {
                new TranableMethodGenerator(classWriter, method, eqlerClass).generate();
            } else {
                new MethodGenerator(classWriter, method, eqlerClass).generate();
            }
        }

        return createBytes();
    }

    private byte[] createBytes() {
        classWriter.visitEnd();
        return classWriter.toByteArray();
    }

    private ClassWriter createClassWriter() {
        final String implSourceName = implName.replace('.', '/');
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        String[] interfaces = {Type.getInternalName(eqlerClass)};
        cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, implSourceName, null, "java/lang/Object", interfaces);
        return cw;
    }

    private void constructor() {
        MethodVisitor mv = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }
}
