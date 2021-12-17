package top.niunaijun.blackobfuscator.core;

import com.googlecode.dex2jar.tools.Dex2jarCmd;
import com.googlecode.dex2jar.tools.Jar2Dex;

import org.jf.DexLib2Utils;

import java.io.File;
import java.util.Arrays;

import top.niunaijun.obfuscator.ObfuscatorConfiguration;

/**
 * Created by Milk on 2021/12/17.
 * * ∧＿∧
 * (`･ω･∥
 * 丶　つ０
 * しーＪ
 * 此处无Bug
 */
public class ObfDex {
    public static void obf(String dir, int depth, String[] obfClass) {
        File tempJar = null;
        File splitDex = null;
        File obfDex = null;
        File file = new File(dir);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null)
                return;
            for (File input : files) {
                if (!input.getAbsolutePath().endsWith(".dex"))
                    return;
                try {
                    tempJar = new File(input.getParent(), System.currentTimeMillis() + "obf.jar");
                    splitDex = new File(input.getParent(), System.currentTimeMillis() + "split.dex");
                    obfDex = new File(input.getParent(), System.currentTimeMillis() + "obf.dex");
                    long l = DexLib2Utils.splitDex(input, splitDex, Arrays.asList(obfClass));
                    if (l <= 0) {
                        System.out.println("No classes found");
                        return;
                    }

                    new Dex2jarCmd(new ObfuscatorConfiguration() {
                        @Override
                        protected int getObfDepth() {
                            return depth;
                        }

                    }).doMain("-f", splitDex.getAbsolutePath(), "-o", tempJar.getAbsolutePath());
                    new Jar2Dex().doMain("-f", "-o", obfDex.getAbsolutePath(), tempJar.getAbsolutePath());
                    DexLib2Utils.mergerAndCoverDexFile(input, obfDex, input);
                } catch (Throwable t) {
                    t.printStackTrace();
                } finally {
                    tempJar.delete();
                    splitDex.delete();
                    obfDex.delete();
                }
            }
        }

    }
}
