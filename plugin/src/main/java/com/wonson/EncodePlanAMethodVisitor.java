package com.wonson;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
public class EncodePlanAMethodVisitor extends EncodeMethodVisitor{
    public EncodePlanAMethodVisitor(int api, MethodVisitor methodVisitor) {
        super(api, methodVisitor);
    }
    @Override
    public void visitLineNumber(int line, Label start) {}

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, org.objectweb.asm.Label start, org.objectweb.asm.Label end, int index) {}
}
