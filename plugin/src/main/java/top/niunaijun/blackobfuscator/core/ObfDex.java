package top.niunaijun.blackobfuscator.core;
import com.googlecode.dex2jar.tools.Dex2jarCmd;
import com.googlecode.dex2jar.tools.Jar2Dex;
import com.wonson.Operator;
import org.jf.DexLib2Utils;
import org.jf.util.TrieTree;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    public static void obf(String dir, int depth, String[] obfClass, String[] blackClass, String mappingFile) {
        File file = new File(dir);
        Mapping mapping = new Mapping(mappingFile);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) return;
            for (File input : files) {
                if (input.isFile()) {
                    handleDex(input, depth, obfClass, blackClass, mapping);
                } else {
                    obf(input.getAbsolutePath(), depth, obfClass, blackClass, mappingFile);
                }
            }
        } else {
            handleDex(file, depth, obfClass, blackClass, mapping);
        }
    }

    private static void handleDex(File input, int depth, String[] obfClass, String[] blackClass, Mapping mapping) {
        if (!input.getAbsolutePath().endsWith(".dex")) return;
        File tempJar = null;
        File splitDex = null;
        File obfDex = null;
        File new_tempJar = null;
        try {
            tempJar = new File(input.getParent(), System.currentTimeMillis() + "obf" + input.getName() + ".jar");
            splitDex = new File(input.getParent(), System.currentTimeMillis() + "split" + input.getName() + ".dex");
            obfDex = new File(input.getParent(), System.currentTimeMillis() + "obf" + input.getName() + ".dex");
            new_tempJar = new File(tempJar.getParent(), "new_" + tempJar.getName());
            List<String> obfClassList = arrayToList(obfClass);
            List<String> blackClassList = arrayToList(blackClass);
            // 解析官方的类名混淆文件 获取白名单中被映射后的类名
            TrieTree whiteListTree = new TrieTree();
            whiteListTree.addAll(obfClassList);
            for (String aClass : mapping.getMapping().keySet()) {
                if (whiteListTree.search(aClass)) {
                    String orig = mapping.get(aClass);
                    if (orig != null) {
                        System.out.println("mapping : " + aClass + " ---> " + orig);
                        obfClassList.add(orig);
                    }
                }
            }
            // 解析官方的类名混淆文件 获取黑名单中被映射后的类名
            TrieTree blackListTree = new TrieTree();
            blackListTree.addAll(blackClassList);
            List<String> tmpBlackClass = new ArrayList<>(blackClassList);
            for (String aClass : tmpBlackClass) {
                if (blackListTree.search(aClass)) {
                    String orig = mapping.get(aClass);
                    if (orig != null) {
                        System.out.println("mapping : " + aClass + " ---> " + orig);
                        blackClassList.add(orig);
                    }
                }
            }

            // 通过dexLib2 解析出在obfClassList&&不在blackClassList中的类 分别编译成smali文件 再合并成新的dex文件
            // 并输出到splitDex指定的路径
            long l = DexLib2Utils.splitDex(input, splitDex, obfClassList, blackClassList);
            if (l <= 0) {
                System.out.println("Obfuscator Class not found");
                return;
            }
            // 使用dex2jar 将.dex转换成.class(.jar)
            // 具体就是把 splitDex->tempJar
            new Dex2jarCmd(new ObfuscatorConfiguration() {
                @Override
                public int getObfDepth() {
                    return depth;
                }

                @Override
                public boolean accept(String className, String methodName) {
                    System.out.println("BlackObf Class: " + className + "#" + methodName);
                    return super.accept(className, methodName);
                }
            }).doMain("-f", splitDex.getAbsolutePath(), "-o", tempJar.getAbsolutePath());
            // 字符串混淆 string->byte[] 异或 + Base64 异或因子随机 内联解密以防hook
            Operator.run(tempJar,new_tempJar,true);
            new Jar2Dex().doMain("-f", "-o", obfDex.getAbsolutePath(), new_tempJar.getAbsolutePath());
            DexLib2Utils.mergerAndCoverDexFile(input, obfDex, input);
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            tempJar.delete();
            new_tempJar.delete();
            splitDex.delete();
            obfDex.delete();
        }
    }

    private static List<String> arrayToList(String[] array) {
        List<String> list = Arrays.asList(array);
        return new ArrayList<>(list);
    }
}
