package com.wonson;
import org.objectweb.asm.*;
import java.io.*;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import static org.objectweb.asm.Opcodes.*;
public class Operator {
    private static Random random = new Random();
    public static int max_local_var = 0;
    public static void run(File input_jar,File output_jar,boolean delete_input){
        try {
            ZipFileHelper zipFileHelper = new ZipFileHelper(input_jar, output_jar);
            List<String> zip_entry_names = zipFileHelper.new_zip_entry_names;
            Iterator<String> iterator = zip_entry_names.iterator();
            while (iterator.hasNext()){
                String entry_name = iterator.next();
                if(entry_name.endsWith(".class")){
                    InputStream entryInputStream = zipFileHelper.getEntryInputStream(entry_name,true);
                    byte[] ret = start(entryInputStream);
                    zipFileHelper.add_entry(entry_name,ret);
                }
            }
            zipFileHelper.commit();
            if(delete_input) {
                input_jar.delete();
            }
            System.out.println("max_local_var:" + max_local_var);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void run(File file){
        if(file.isDirectory()){
            FileFilter fileFilter = new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (!pathname.isDirectory() && !pathname.getName().endsWith(".class")) {
                        return false;
                    } else {
                        return true;
                    }
                }
            };
            File[] files = file.listFiles(fileFilter);
            for (File listFile : files) {
                 run(listFile);
            }
        }else {
            start(file);
        }
    }

    private static void start(File classFile) {
        try {
            FileInputStream fileInputStream = new FileInputStream(classFile);
            byte[] ret = start(fileInputStream);
            FileOutputStream fileOutputStream = new FileOutputStream(classFile);
            fileOutputStream.write(ret);
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static byte[] start(InputStream inputStream) throws IOException {
            byte[] bytes = ZipFileHelper.inputStreamToByteArray(inputStream);
            return start(bytes);
    }

    private static byte[] start(byte[] classByteCode){
        byte[] back_up = new byte[classByteCode.length];
        for (int index = 0; index < back_up.length; index++){
            back_up[index] = classByteCode[index];
        }
        ClassReader classReader = new ClassReader(classByteCode);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        ClassVisitor classVisitor = new ClassVisitor(ASM7,classWriter){
            private int var = 256;
            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                super.visit(version, access, name, signature, superName, interfaces);
                System.out.println("encoding:" + name);
            }
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
                MethodVisitor myMethodVisitor = new MethodVisitor(ASM7, methodVisitor) {
                    @Override
                    public void visitLdcInsn(Object cst) {
                        if(cst != null && cst instanceof String){
                            byte key = (byte) random.nextInt();
                            String target = String.class.cast(cst);
                            if(target.length() == 0){
                                mv.visitLdcInsn(cst);
                                return;
                            }
                            byte[] bytes = target.getBytes();
                            for(int i = 0;i < bytes.length; i++) bytes[i] ^= key;
                            byte[] encode = Base64.getEncoder().encode(bytes);
                            //创建数组
                            mv.visitIntInsn(BIPUSH,encode.length);
                            mv.visitIntInsn(NEWARRAY, T_BYTE);
                            mv.visitInsn(DUP);
                            //填充数值
                            for(int index = 0; index < encode.length;index++){
                                mv.visitIntInsn(BIPUSH,index);
                                mv.visitIntInsn(BIPUSH,encode[index]);
                                mv.visitInsn(BASTORE);
                                if(index < encode.length - 1) mv.visitInsn(DUP);
                                else mv.visitVarInsn(ASTORE,var + 1);
                            }
                            //函数调用
                            mv.visitMethodInsn(INVOKESTATIC, "java/util/Base64", "getDecoder", "()Ljava/util/Base64$Decoder;", false);
                            mv.visitVarInsn(ALOAD, var + 1);
                            mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Base64$Decoder", "decode", "([B)[B", false);
                            mv.visitVarInsn(ASTORE,var + 1);
                            Label l1 = new Label();
                            mv.visitLabel(l1);

                            mv.visitInsn(ICONST_0);
                            mv.visitVarInsn(ISTORE, var + 2);
                            Label l2 = new Label();
                            mv.visitLabel(l2);

                            mv.visitFrame(Opcodes.F_APPEND, 2, new Object[]{"[B", Opcodes.INTEGER}, 0, null);
                            mv.visitVarInsn(ILOAD, var + 2);
                            mv.visitVarInsn(ALOAD, var + 1);
                            mv.visitInsn(ARRAYLENGTH);
                            Label l3 = new Label();
                            mv.visitJumpInsn(IF_ICMPGE, l3);
                            Label l4 = new Label();
                            mv.visitLabel(l4);
                            mv.visitVarInsn(ALOAD, var + 1);
                            mv.visitVarInsn(ILOAD, var + 2);
                            mv.visitInsn(DUP2);
                            mv.visitInsn(BALOAD);
                            mv.visitIntInsn(BIPUSH,key);
                            mv.visitInsn(IXOR);
                            mv.visitInsn(I2B);
                            mv.visitInsn(BASTORE);
                            Label l5 = new Label();
                            mv.visitLabel(l5);
                            mv.visitIincInsn(var + 2, 1);
                            mv.visitJumpInsn(GOTO, l2);
                            mv.visitLabel(l3);
                            mv.visitFrame(Opcodes.F_CHOP, 1, null, 0, null);
                            mv.visitTypeInsn(NEW, "java/lang/String");
                            mv.visitInsn(DUP);
                            mv.visitVarInsn(ALOAD, var + 1);
                            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "([B)V", false);
                            return;
                        }
                        super.visitLdcInsn(cst);
                    }

                    @Override
                    public void visitLineNumber(int line, Label start) {}

                    @Override
                    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {}

                    @Override
                    public void visitMaxs(int maxStack, int maxLocals) {
                        super.visitMaxs(maxStack,maxLocals + 2);
                        if(max_local_var < maxLocals){
                            max_local_var = maxLocals;
                        }
                    }
                };
                return myMethodVisitor;
            }
        };
        classReader.accept(classVisitor,ClassReader.SKIP_DEBUG);
        try {
            byte[] bytes = classWriter.toByteArray();
            return bytes;
        }catch (Exception exception){
            exception.printStackTrace();
            return back_up;
        }
    }
}