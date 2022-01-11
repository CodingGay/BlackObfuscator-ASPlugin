package top.niunaijun.blackobfuscator

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.internal.dsl.BuildType
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
                android.applicationVariants.all(new Action<ApplicationVariant>() {
                    @Override
                    void execute(ApplicationVariant applicationVariant) {
                        def name = upperCaseFirst(applicationVariant.buildType.name)
                        def names = [applicationVariant.buildType.name, name]
                        for (String p : names) {
                            addOtherTask(tasks, p)
                        }
                    }
                })
                android.buildTypes.all(new Action<BuildType>() {
                    @Override
                    void execute(BuildType buildType) {
                        def name = upperCaseFirst(buildType.name)
                        def names = [buildType.name, name]
                        for (String p : names) {
                            addOtherTask(tasks, p)
                        }
                    }
                })
                android.productFlavors.all(new Action<ProductFlavor>() {
                    @Override
                    void execute(ProductFlavor productFlavor) {
                        def name = upperCaseFirst(productFlavor.name)
                        def names = [productFlavor.name, name]
                        for (String p : names) {
                            addOtherTask(tasks, p)
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

    private void addOtherTask(List<Task> tasks, String name) {
        addTask("mergeDex${name}Release", tasks)
        addTask("mergeDex${name}Debug", tasks)
        addTask("mergeLibDex${name}Debug", tasks)
        addTask("mergeProjectDex${name}Debug", tasks)

        addTask("transformDexArchiveWithDexMergerFor${name}Debug", tasks)
        addTask("transformDexArchiveWithDexMergerFor${name}Release", tasks)

        addTask("minify${name}ReleaseWithR8", tasks)
        addTask("minify${name}DebugWithR8", tasks)
    }

    private String upperCaseFirst(String val) {
        char[] arr = val.toCharArray();
        arr[0] = Character.toUpperCase(arr[0]);
        return new String(arr);
    }

    private void addTask(String name, List<Task> tasks) {
        try {
            //Protected code
            Task task = mProject.tasks.getByName(name)
            if (!tasks.contains(task)) {
                tasks.add(task)
                println("add Task $name")
            }
        } catch(UnknownTaskException e1) {
            //Catch block
        }
    }
}