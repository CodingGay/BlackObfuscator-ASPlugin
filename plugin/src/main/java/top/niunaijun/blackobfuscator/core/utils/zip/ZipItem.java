package top.niunaijun.blackobfuscator.core.utils.zip;

import java.io.InputStream;

/**
 * Created by sunwanquan on 2019/6/18.
 * * ∧＿∧
 * (`･ω･∥
 * 丶　つ０
 * しーＪ
 * 此处无Bug
 */
public class ZipItem {
    private InputStream stream;
    private String zipEntryName;

    public ZipItem() {
    }

    public ZipItem(InputStream stream, String zipEntryName) {
        this.stream = stream;
        this.zipEntryName = zipEntryName;
    }

    public InputStream getStream() {
        return stream;
    }

    public void setStream(InputStream stream) {
        this.stream = stream;
    }

    public String getZipEntryName() {
        return zipEntryName;
    }

    public void setZipEntryName(String zipEntryName) {
        this.zipEntryName = zipEntryName;
    }
}
