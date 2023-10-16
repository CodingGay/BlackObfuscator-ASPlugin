package com.wonson;
import org.objectweb.asm.MethodVisitor;
import java.util.Base64;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
public class EncodePlanBMethodVisitor extends EncodePlanAMethodVisitor{

    public EncodePlanBMethodVisitor(int api, MethodVisitor methodVisitor) {
        super(api, methodVisitor);
    }

    @Override
    public void visitLdcInsn(Object cst) {
        if(cst != null && cst instanceof String){
            String target = String.class.cast(cst);
            //���˵����ַ���
            if(target.length() == 0) {
                mv.visitLdcInsn(cst);
            } else {
                // ����һ������ ���������ý�ջ
                mv.visitTypeInsn(NEW, "java/lang/String");
                mv.visitInsn(DUP);
                mv.visitMethodInsn(INVOKESTATIC, "java/util/Base64", "getDecoder", "()Ljava/util/Base64$Decoder;", false);
                byte[] encode = Base64.getEncoder().encode(target.getBytes());
                //��������
                // BIPUSH: ��һ��byte�ͳ���ֵ������ջ��
                // mv.visitIntInsn(BIPUSH,encode.length);
                mv.visitLdcInsn(encode.length);
                // ջ����ֵ��count����Ϊ���鳤�ȣ�����һ�����������顣ջ����ֵ��ջ���������ý�ջ��
                mv.visitIntInsn(NEWARRAY,T_BYTE);
                //����ջ����ֵ�����Ҹ���ֵ��ջ
                mv.visitInsn(DUP);
                //�����ֵ
                for(int index = 0; index < encode.length;index++){
                    mv.visitLdcInsn(index);
                    mv.visitIntInsn(BIPUSH,encode[index]);
                    //��ջ��boolean��byte����ֵ����ָ�������ָ���±괦
                    mv.visitInsn(BASTORE);
                    if(index < encode.length - 1) {
                        mv.visitInsn(DUP);
                    }
                }
                // ��������
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Base64$Decoder", "decode", "([B)[B", false);
                // ���ó��๹�췽����ʵ����ʼ��������˽�з���
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "([B)V", false);
            }
        } else {
            super.visitLdcInsn(cst);
        }
    }
}
