package org.n3r.eql.eqler.generators;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import org.n3r.eql.eqler.annotations.EqlerConfig;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;

import java.io.FileOutputStream;

import static org.objectweb.asm.Opcodes.*;

@SuppressWarnings("unchecked")
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

        diagnose(bytes);

        return defineClass(bytes);
    }

    private void diagnose(byte[] bytes) {
        val eqlerConfig = eqlerClass.getAnnotation(EqlerConfig.class);
        if (eqlerConfig == null || !eqlerConfig.createClassFileForDiagnose())
            return;

        writeClassFile4Diagnose(bytes, eqlerClass.getSimpleName() + "Impl.class");
    }

    @SneakyThrows
    private void writeClassFile4Diagnose(byte[] bytes, String fileName) {
        @Cleanup val fos = new FileOutputStream(fileName);
        fos.write(bytes);
    }

    private Class<? extends T> defineClass(byte[] bytes) {
        val loader = new EqlerClassLoader(eqlerClass.getClassLoader());
        return (Class<? extends T>) loader.defineClass(implName, bytes);
    }

    private byte[] createEqlImplClassBytes() {
        constructor();

        for (val method : eqlerClass.getMethods()) {
            Generatable generator = TranableMethodGenerator.isEqlTranableMethod(method)
                    ? new TranableMethodGenerator(classWriter, method, eqlerClass)
                    : new MethodGenerator(classWriter, method, eqlerClass);
            generator.generate();
        }

        return createBytes();
    }

    private byte[] createBytes() {
        classWriter.visitEnd();
        return classWriter.toByteArray();
    }

    private ClassWriter createClassWriter() {
        val implSourceName = implName.replace('.', '/');
        val cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        String[] interfaces = {Type.getInternalName(eqlerClass)};
        cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, implSourceName,
                null, "java/lang/Object", interfaces);
        return cw;
    }

    private void constructor() {
        val mv = classWriter.visitMethod(ACC_PUBLIC,
                "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL,
                "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }
}
