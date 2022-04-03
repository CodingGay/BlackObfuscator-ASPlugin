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
            //过滤掉空字符串
            if(target.length() == 0) {
                mv.visitLdcInsn(cst);
            } else {
                // 创建一个对象 并且其引用进栈
                mv.visitTypeInsn(NEW, "java/lang/String");
                mv.visitInsn(DUP);
                mv.visitMethodInsn(INVOKESTATIC, "java/util/Base64", "getDecoder", "()Ljava/util/Base64$Decoder;", false);
                byte[] encode = Base64.getEncoder().encode(target.getBytes());
                //创建数组
                // BIPUSH: 将一个byte型常量值推送至栈顶
                // mv.visitIntInsn(BIPUSH,encode.length);
                mv.visitLdcInsn(encode.length);
                // 栈顶数值（count）作为数组长度，创建一个引用型数组。栈顶数值出栈，数组引用进栈。
                mv.visitIntInsn(NEWARRAY,T_BYTE);
                //复制栈顶数值，并且复制值进栈
                mv.visitInsn(DUP);
                //填充数值
                for(int index = 0; index < encode.length;index++){
                    mv.visitLdcInsn(index);
                    mv.visitIntInsn(BIPUSH,encode[index]);
                    //将栈顶boolean或byte型数值存入指定数组的指定下标处
                    mv.visitInsn(BASTORE);
                    if(index < encode.length - 1) {
                        mv.visitInsn(DUP);
                    }
                }
                // 函数调用
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Base64$Decoder", "decode", "([B)[B", false);
                // 调用超类构造方法、实例初始化方法、私有方法
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "([B)V", false);
            }
        } else {
            super.visitLdcInsn(cst);
        }
    }
}
