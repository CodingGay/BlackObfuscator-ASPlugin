package top.niunaijun.blackobfuscator

import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.dsl.ProductFlavor
import com.android.build.gradle.internal.tasks.DexMergingTask
import org.gradle.api.*
import org.gradle.api.internal.file.DefaultFilePropertyFactory
import top.niunaijun.blackobfuscator.core.ObfDex

public class ObfPlugin implements Plugin<Project> {
    private String PLUGIN_NAME = "BlackObfuscator"
    private Project mProject
    public static BlackObfuscatorExtension sObfuscatorExtension

    void apply(Project project) {
        this.mProject = project
        def android = project.extensions.findByType(AppExtension)
        project.configurations.create(PLUGIN_NAME).extendsFrom(project.configurations.implementation)
        sObfuscatorExtension = project.extensions.create(PLUGIN_NAME, BlackObfuscatorExtension, project)

        project.afterEvaluate {
            System.out.println("=====BlackObfuscator=====")
            System.out.println(sObfuscatorExtension.toString())
            System.out.println("=========================")
        }

        project.afterEvaluate { ->
            if (!sObfuscatorExtension.enabled) {
                return
            }
            def action = new Action<Task>() {
                @Override
                void execute(Task task) {
                    task.getOutputs().getFiles().collect().each() { element ->
                        def file = new File(element.toString())
                        ObfDex.obf(file.getAbsolutePath(),
                                sObfuscatorExtension.depth, sObfuscatorExtension.obfClass, sObfuscatorExtension.blackClass)
                    }
                }
            }
            List<Task> tasks = new ArrayList<>()
            addTask("mergeDexRelease", tasks)
            addTask("mergeDexDebug", tasks)
            addTask("mergeLibDexDebug", tasks)
            addTask("mergeProjectDexDebug", tasks)
            addTask("transformDexArchiveWithDexMergerForDebug", tasks)
            addTask("transformDexArchiveWithDexMergerForRelease", tasks)
            addTask("minifyReleaseWithR8", tasks)
            addTask("minifyDebugWithR8", tasks)

            if (android != null) {
                android.productFlavors.all(new Action<ProductFlavor>() {
                    @Override
                    void execute(ProductFlavor productFlavor) {
                        println("ProductFlavor: " + name)
                        def name = upperCaseFirst(productFlavor.name)
                        def names = [productFlavor.name, name]
                        for (String p : names) {
                            addTask("mergeDex${p}Release", tasks)
                            addTask("mergeDex${p}Debug", tasks)
                            addTask("mergeLibDex${p}Debug", tasks)
                            addTask("mergeProjectDex${p}Debug", tasks)

                            addTask("transformDexArchiveWithDexMergerFor${p}Debug", tasks)
                            addTask("transformDexArchiveWithDexMergerFor${p}Release", tasks)

                            addTask("minify${p}ReleaseWithR8", tasks)
                            addTask("minify${p}DebugWithR8", tasks)
                        }
                    }
                })
            }

            for (Task task : tasks) {
                task.doLast(action)
            }
            if (tasks.isEmpty()) {
                System.err.println("This gradle version is not applicable. Please submit issues in https://github.com/CodingGay/BlackObfuscator-ASPlugin")
            }
        }
    }

    private String upperCaseFirst(String val) {
        char[] arr = val.toCharArray();
        arr[0] = Character.toUpperCase(arr[0]);
        return new String(arr);
    }

    private void addTask(String name, List<Task> tasks) {
        try {
            //Protected code
            tasks.add(mProject.tasks.getByName(name))
            println("add Task $name")
        } catch(UnknownTaskException e1) {
            //Catch block
        }
    }
}