package com.wonson;
import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
public class ZipFileHelper {
    protected List<String> old_zip_entry_names;
    protected List<String> new_zip_entry_names;
    protected ZipFile op_zip_file;
    protected ZipOutputStream zipOutputStream;
    protected String zip_input_file_path;

    public ZipFileHelper(File zip_input_file,File zip_output_file) throws IOException {
        this.zip_input_file_path = zip_input_file.getAbsolutePath();
        this.op_zip_file = new ZipFile(zip_input_file);
        this.zipOutputStream = new ZipOutputStream(new FileOutputStream(zip_output_file));
        this.old_zip_entry_names = new ArrayList<>();
        this.new_zip_entry_names = new ArrayList<>();
        Enumeration<? extends ZipEntry> entries = op_zip_file.entries();
        while (entries.hasMoreElements()){
            ZipEntry zipEntry = entries.nextElement();
            String entryName = zipEntry.getName();
            old_zip_entry_names.add(entryName);
            new_zip_entry_names.add(entryName);
        }
    }

    public void commit() throws IOException {
        Iterator<String> iterator = old_zip_entry_names.iterator();
        while (iterator.hasNext()){
            String next = iterator.next();
            ZipEntry entry = op_zip_file.getEntry(next);
            ZipEntry zipEntry = new ZipEntry(next);
            zipOutputStream.putNextEntry(zipEntry);
            InputStream inputStream = op_zip_file.getInputStream(entry);
            write_to_outputStream_from_inputStream(inputStream,zipOutputStream,false);
            zipOutputStream.closeEntry();
        }
        zipOutputStream.close();
        op_zip_file.close();
    }

    public boolean remove_entry(String entry_name){
       if(entry_name.startsWith("/")){
           entry_name = entry_name.substring(1);
        }
        return old_zip_entry_names.remove(entry_name);
    }

    public void remove_all_entry(){
        old_zip_entry_names.clear();
    }

    public void recover_entry(String entry_name){
        if(entry_name.startsWith("/")) entry_name = entry_name.substring(1);
        old_zip_entry_names.add(entry_name);
    }

    public void add_entry(String entry_name,InputStream inputStream) throws IOException {
        entry_name = filter(entry_name);
        ZipEntry zipEntry = new ZipEntry(entry_name);
        zipOutputStream.putNextEntry(zipEntry);
        write_to_outputStream_from_inputStream(inputStream,zipOutputStream,false);
        zipOutputStream.closeEntry();
    }

    public void add_entry(String entry_name,String text) throws IOException {
        entry_name = filter(entry_name);
        ZipEntry zipEntry = new ZipEntry(entry_name);
        zipOutputStream.putNextEntry(zipEntry);
        zipOutputStream.write(text.getBytes());
        zipOutputStream.closeEntry();
    }

    public void add_entry(String entry_name,byte[] data) throws IOException {
        entry_name = filter(entry_name);
        ZipEntry zipEntry = new ZipEntry(entry_name);
        zipOutputStream.putNextEntry(zipEntry);
        zipOutputStream.write(data);
        zipOutputStream.closeEntry();
    }

    public byte[] getEntryByteArray(String entry_name,boolean remove_entry) throws IOException {
        InputStream entryInputStream = getEntryInputStream(entry_name,remove_entry);
        byte[] bytes = inputStreamToByteArray(entryInputStream);
        return bytes;
    }

    public InputStream getEntryInputStream(String entry_name,boolean remove_entry) throws IOException {
        if(entry_name.startsWith("/")) entry_name.substring(1);
        ZipEntry zipEntry = op_zip_file.getEntry(entry_name);
        InputStream inputStream = op_zip_file.getInputStream(zipEntry);
        if(remove_entry) remove_entry(entry_name);
        return inputStream;
    }

    private void write_to_outputStream_from_inputStream(InputStream inputStream, OutputStream outputStream,boolean close_outputStream) throws IOException {
        int offset;
        byte[] buffer = new byte[1024];
        while ((offset = inputStream.read(buffer)) != -1) outputStream.write(buffer,0,offset);
        inputStream.close();
        if(close_outputStream) outputStream.close();
    }

    private String filter(String entry_name) throws IOException {
        if(entry_name.startsWith("/")) entry_name = entry_name.substring(1);
        if(old_zip_entry_names.contains(entry_name)){
            boolean ok = remove_entry(entry_name);
            String failed_to_remove = new StringBuilder("failed to remove ").append(entry_name).toString();
            if(!ok) throw new IOException(failed_to_remove);
        }
        return entry_name;
    }

    public static byte[] inputStreamToByteArray(InputStream inputStream) throws IOException {
        byte[] result = null;
        byte[] buffer = new byte[1024];
        int offset;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        while ((offset = inputStream.read(buffer)) != -1) byteArrayOutputStream.write(buffer,0,offset);
        result = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.close();
        inputStream.close();
        return result;
    }
}