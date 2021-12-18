package top.niunaijun.blackobfuscator

import com.android.build.gradle.internal.tasks.DexMergingTask
import com.android.builder.model.ProductFlavor
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project

import com.android.build.gradle.AppExtension
import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.api.internal.file.DefaultFilePropertyFactory
import top.niunaijun.blackobfuscator.core.ObfDex

public class ObfPlugin implements Plugin<Project> {
    private String PLUGIN_NAME = "BlackObfuscator"
    private Project mProject
    public static BlackObfuscatorExtension sObfuscatorExtension

    void apply(Project project) {
        this.mProject = project
        def android = project.extensions.findByType(AppExtension)
        project.configurations.create(PLUGIN_NAME).extendsFrom(project.configurations.compile)
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
                    DexMergingTask dexMergingTask = task
                    def file = dexMergingTask.outputDir
                    File finalFile
                    if (file instanceof File) {
                        finalFile = file
                    } else if (file instanceof DefaultFilePropertyFactory.DefaultDirectoryVar) {
                        DefaultFilePropertyFactory.DefaultDirectoryVar defaultDirectoryVar = dexMergingTask.outputDir
                        finalFile = defaultDirectoryVar.asFile.get()
                    } else {
                        throw new RuntimeException("BlackObfuscator not support the gradle version!")
                    }
                    ObfDex.obf(finalFile.getAbsolutePath(),
                            sObfuscatorExtension.depth, sObfuscatorExtension.obfClass)
                }
            }
            List<Task> tasks = new ArrayList<>()
            List<Task> tasks2 = new ArrayList<>()
            addTask("mergeDexRelease", tasks)
            addTask("mergeProjectDexDebug", tasks)

            addTask("transformDexArchiveWithDexMergerForDebug", tasks2)
            addTask("transformDexArchiveWithDexMergerForRelease", tasks2)

            if (android != null) {
                android.productFlavors.all(new Action<com.android.build.gradle.internal.dsl.ProductFlavor>() {
                    @Override
                    void execute(com.android.build.gradle.internal.dsl.ProductFlavor productFlavor) {
                        addTask("mergeProjectDex${productFlavor.name}Debug", tasks)
                        addTask("mergeDex${productFlavor.name}Release", tasks)

                        addTask("transformDexArchiveWithDexMergerFor${productFlavor.name}Debug", tasks2)
                        addTask("transformDexArchiveWithDexMergerFor${productFlavor.name}Release", tasks2)
                    }
                })
            }

            for (Task task : tasks) {
                task.doLast(action)
            }

            def action2 = new Action<Task>() {
                @Override
                void execute(Task task) {
                    task.getOutputs().getFiles().collect().each() { element ->
                        def file = new File(element.toString())
                        ObfDex.obf(file.getAbsolutePath(),
                                sObfuscatorExtension.depth, sObfuscatorExtension.obfClass)
                    }
                }
            }
            for (Task task : tasks2) {
                task.doLast(action2)
            }

//            if (tasks2.isEmpty() && tasks.isEmpty()) {
//                throw new RuntimeException("This gradle version is not applicable. Please submit issues in https://github.com/CodingGay/BlackObfuscator-ASPlugin")
//            }
        }
    }

    private void addTask(String name, List<Task> tasks) {
        try {
            println("add Task $name")
            //Protected code
            tasks.add(mProject.tasks.getByName(name))
        } catch(UnknownTaskException e1) {
            //Catch block
        }
    }
}