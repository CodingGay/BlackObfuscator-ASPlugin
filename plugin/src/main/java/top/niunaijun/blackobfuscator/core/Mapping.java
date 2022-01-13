package top.niunaijun.blackobfuscator.core;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Milk on 2022/1/12.
 * * ∧＿∧
 * (`･ω･∥
 * 丶　つ０
 * しーＪ
 * 此处无Bug
 */
public class Mapping {
    private File mFile;
    private final Map<String, String> mapping = new HashMap<>();

    public Mapping(String file) {
        if (file == null)
            return;
        mFile = new File(file);
        parser();
    }

    private void parser() {
        if (mFile == null || !mFile.exists())
            return;
        try {
            List<String> strings = FileUtils.readLines(mFile, Charset.defaultCharset());
            for (String string : strings) {
                if (string.startsWith("#") || string.startsWith(" ")) {
                    continue;
                }
                String[] split = string.split(" -> ");
                if (split.length != 2) {
                    continue;
                }
//                System.out.println("add mapping : " + string);
                mapping.put(split[0], split[1].substring(0, split[1].length() - 1));
            }
        } catch (IOException ignored) {
        }
    }

    public String get(String origClazz) {
        return mapping.get(origClazz);
    }

    public Map<String, String> getMapping() {
        return mapping;
    }
}
