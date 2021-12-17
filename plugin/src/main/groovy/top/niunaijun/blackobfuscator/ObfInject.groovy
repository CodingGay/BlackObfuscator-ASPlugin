package top.niunaijun.blackobfuscator

import com.android.dex.util.FileUtils
import com.android.dx.command.Main
import com.googlecode.d2j.converter.IR2JConverter
import com.googlecode.d2j.converter.J2IRConverter
import com.googlecode.dex2jar.ir.IrMethod
import com.googlecode.dex2jar.ir.ts.AggTransformer
import com.googlecode.dex2jar.ir.ts.CleanLabel
import com.googlecode.dex2jar.ir.ts.DeadCodeTransformer
import com.googlecode.dex2jar.ir.ts.ExceptionHandlerTrim
import com.googlecode.dex2jar.ir.ts.Ir2JRegAssignTransformer
import com.googlecode.dex2jar.ir.ts.NewTransformer
import com.googlecode.dex2jar.ir.ts.NpeTransformer
import com.googlecode.dex2jar.ir.ts.RemoveConstantFromSSA
import com.googlecode.dex2jar.ir.ts.RemoveLocalFromSSA
import com.googlecode.dex2jar.ir.ts.TypeTransformer
import com.googlecode.dex2jar.ir.ts.UnSSATransformer
import com.googlecode.dex2jar.ir.ts.VoidInvokeTransformer
import com.googlecode.dex2jar.ir.ts.ZeroTransformer
import com.googlecode.dex2jar.ir.ts.array.FillArrayTransformer
import com.googlecode.dex2jar.tools.Dex2jarCmd
import org.gradle.api.Project
import org.jf.CloseUtils
import org.jf.DexLib2Utils
import org.objectweb.asm2.ClassReader
import org.objectweb.asm2.ClassWriter
import org.objectweb.asm2.tree.ClassNode
import org.objectweb.asm2.tree.MethodNode
import top.niunaijun.blackobfuscator.core.utils.zip.UtilsZip
import top.niunaijun.obfuscator.ObfuscatorConfiguration

class ObfInject {
    private Project mProject
    private BlackObfuscatorExtension mObfuscatorExtension

    protected final CleanLabel T_cleanLabel = new CleanLabel();
    protected final Ir2JRegAssignTransformer T_ir2jRegAssign = new Ir2JRegAssignTransformer();
    protected final NewTransformer T_new = new NewTransformer();
    protected final RemoveConstantFromSSA T_removeConst = new RemoveConstantFromSSA();
    protected final RemoveLocalFromSSA T_removeLocal = new RemoveLocalFromSSA();
    protected final ExceptionHandlerTrim T_trimEx = new ExceptionHandlerTrim();
    protected final TypeTransformer T_type = new TypeTransformer();
    protected final DeadCodeTransformer T_deadCode = new DeadCodeTransformer();
    protected final FillArrayTransformer T_fillArray = new FillArrayTransformer();
    protected final AggTransformer T_agg = new AggTransformer();
    protected final UnSSATransformer T_unssa = new UnSSATransformer();
    protected final ZeroTransformer T_zero = new ZeroTransformer();
    protected final VoidInvokeTransformer T_voidInvoke = new VoidInvokeTransformer();
    protected final NpeTransformer T_npe = new NpeTransformer();

    ObfInject(Project project) {
        mProject = project
        mObfuscatorExtension = ObfPlugin.sObfuscatorExtension
    }

    void addJar(String jar) {
    }

    void inject(String path) {
        if (!mObfuscatorExtension.enabled) {
            return;
        }

        File dir = new File(path)

        if (dir.isDirectory()) {
            dir.eachFileRecurse { File file ->
                if (file.isFile() && file.getAbsolutePath().endsWith(".class")) {
                    byte[] data = FileUtils.readFile(file)
                    ClassNode cn = readClassNode(data)
                    reBuildClass(cn)
                    createFile(file.getAbsolutePath(), toByteArray(cn))
                }
            }
            fixClass(dir)
        }
    }

    private void fixClass(File dir) {
        File outDex = new File(dir, "obf.dex")
        File fixDex = new File(dir, "obf_fix.dex")
        File outJar = new File(dir, "obf.jar")
        outDex.delete()
        fixDex.delete()
        outJar.delete()
        Main.main("--dex", "--output=" + outDex.getAbsolutePath(), dir.getAbsolutePath())
        if (outDex.exists()) {
            DexLib2Utils.saveDex(outDex, fixDex)
        }

        if (fixDex.exists()) {
            new Dex2jarCmd(null).doMain(
                    "-f",
                    "-o", outJar.getAbsolutePath(),
                    fixDex.getAbsolutePath());
        }
        if (outJar.exists()) {
            UtilsZip.release(outJar.getAbsolutePath(), dir.getAbsolutePath(), new UtilsZip.UnzipCallback() {
                @Override
                boolean accept(String name) {
                    def newName = name.replace("/", ".")
                    for (String accept : mObfuscatorExtension.obfClass) {
                        if (newName.contains(accept)) {
                            return true
                        }
                    }
                    return false
                }
            })
        }
        outDex.delete()
        fixDex.delete()
        outJar.delete()
    }

    private void reBuildClass(ClassNode cn) {
        for (Iterator<MethodNode> it = cn.methods.iterator(); it.hasNext(); ) {
            MethodNode m = it.next();
            try {
                IrMethod irMethod = J2IRConverter.convert(cn.name, m);
                opt(irMethod);
                // convert ir to m3
                MethodNode m3 = new MethodNode();
                m3.tryCatchBlocks = new ArrayList<>();
                new IR2JConverter()
                        .ir(irMethod)
                        .asm(m3)
                        .obf(new ObfuscatorConfiguration() {
                            @Override
                            protected int getObfDepth() {
                                return mObfuscatorExtension.depth;
                            }

                            @Override
                            protected boolean accept(String className, String methodName) {
                                for (String accept : mObfuscatorExtension.obfClass) {
                                    if (className.contains(accept)) {
                                        return true
                                    }
                                }
                                return false
                            }
                        })
                        .convert();

                // copy back m3 to m
                m.maxLocals = -1;
                m.maxLocals = -1;
                m.instructions = m3.instructions;
                m.tryCatchBlocks = m3.tryCatchBlocks;
                m.localVariables = null;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    void opt(IrMethod irMethod) {
        T_deadCode.transform(irMethod);
        T_cleanLabel.transform(irMethod);
        T_removeLocal.transform(irMethod);
        T_removeConst.transform(irMethod);
        T_zero.transform(irMethod);
        if (T_npe.transformReportChanged(irMethod)) {
            T_deadCode.transform(irMethod);
            T_removeLocal.transform(irMethod);
            T_removeConst.transform(irMethod);
        }
        T_new.transform(irMethod);
        T_fillArray.transform(irMethod);
        T_agg.transform(irMethod);
        T_voidInvoke.transform(irMethod);

        T_type.transform(irMethod);
        T_unssa.transform(irMethod);
        T_trimEx.transform(irMethod);
        T_ir2jRegAssign.transform(irMethod);
    }

    private byte[] toByteArray(ClassNode cn) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cn.accept(cw);
        return cw.toByteArray();
    }


    private ClassNode readClassNode(byte[] data) {
        ClassReader cr = new ClassReader(data);
        ClassNode cn = new ClassNode();
        cr.accept(cn, ClassReader.EXPAND_FRAMES | ClassReader.SKIP_FRAMES);
        return cn
    }

    private static boolean createFile(String path, byte[] content) {
        FileOutputStream fos = null;
        try {
            File file = new File(path);
            if (file.exists()) {
                boolean del = file.delete();
                if (!del) {
                    return false;
                }
            }
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            fos = new FileOutputStream(path);
            fos.write(content);
            fos.flush();
            return file.exists();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            CloseUtils.close(fos);
        }
    }
}
