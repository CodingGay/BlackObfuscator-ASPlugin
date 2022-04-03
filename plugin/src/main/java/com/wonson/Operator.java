package com.wonson;
import org.objectweb.asm.*;
import java.io.*;
import java.util.*;
import static org.objectweb.asm.Opcodes.*;
public class Operator {
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
        ClassVisitor classVisitor = new EncodeClassVisitor(ASM7,classWriter,EncodePlanBMethodVisitor.class);
        classReader.accept(classVisitor,ClassReader.SKIP_DEBUG);
        try {
            return classWriter.toByteArray();
        }catch (Exception exception){
            exception.printStackTrace();
            return back_up;
        }
    }
}