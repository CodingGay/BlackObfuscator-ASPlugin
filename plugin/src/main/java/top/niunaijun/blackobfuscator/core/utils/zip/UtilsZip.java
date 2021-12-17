package top.niunaijun.blackobfuscator.core.utils.zip;


import org.apache.commons.io.FileUtils;
import org.jf.CloseUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;


/**
 * Created by sunwanquan on 2019/6/18.
 * * ∧＿∧
 * (`･ω･∥
 * 丶　つ０
 * しーＪ
 * 此处无Bug
 */
public class UtilsZip {

    /**
     * 解压ZIP
     *
     * @param zipPath zip路径
     * @param outPath 输出目录
     * @return 是否成功
     */
    public static boolean release(String zipPath, String outPath, UnzipCallback callback) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            File out = new File(outPath);
            if (out.exists()) {
                if (out.isFile())
                    throw new RuntimeException("outPath is a file!");
            } else {
                out.mkdirs();
            }

            ZipFile zipFile = new ZipFile(zipPath);
            Enumeration entries = zipFile.getEntries();
            while (entries.hasMoreElements()) {
                try {
                    ZipEntry zipEntry = (ZipEntry) entries.nextElement();
                    if (callback != null) {
                        if (!callback.accept(zipEntry.getName())) {
                            continue;
                        }
                    }
                    File outFile = new File(outPath + File.separator + zipEntry.getName());

                    if (zipEntry.isDirectory()) {
                        outFile.mkdirs();
                    } else {
                        if (!outFile.getParentFile().exists()) {
                            outFile.getParentFile().mkdirs();
                        }
                        outFile.createNewFile();
                        inputStream = zipFile.getInputStream(zipEntry);
                        outputStream = new FileOutputStream(outFile);
                        byte[] bytes = new byte[1024 * 5];
                        int read;
                        while ((read = inputStream.read(bytes)) != -1) {
                            outputStream.write(bytes, 0, read);
                        }
                    }
                } finally {
                    CloseUtils.close(inputStream, outputStream);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            CloseUtils.close(inputStream, outputStream);
        }
    }

    public interface UnzipCallback {
        boolean accept(String name);
    }

    private static boolean isExist(List<String> strings, String s) {
        for (String str : strings) {
            if (s.startsWith(str)) {
                return true;
            }
        }
        return false;
    }

    public static InputStream getZipEntryInputStream(String zipFile, String entryName) {
        ZipFile zipFile1 = null;
        try {
            zipFile1 = new ZipFile(zipFile);
            return zipFile1.getInputStream(zipFile1.getEntry(entryName));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
//            CloseUtils.close(zipFile1);
        }
        return null;
    }
}
