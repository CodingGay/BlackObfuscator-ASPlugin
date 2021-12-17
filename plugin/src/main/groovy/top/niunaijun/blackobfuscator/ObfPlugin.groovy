package top.niunaijun.blackobfuscator

import com.android.build.gradle.internal.tasks.DexMergingTask
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project

import com.android.build.gradle.AppExtension
import org.gradle.api.Task
import org.gradle.api.internal.file.DefaultFilePropertyFactory
import top.niunaijun.blackobfuscator.core.ObfDex

public class ObfPlugin implements Plugin<Project> {
    private String PLUGIN_NAME = "BlackObfuscator"
    private Project mProject
    public static BlackObfuscatorExtension sObfuscatorExtension

    void apply(Project project) {
        this.mProject = project
        def android = project.extensions.getByType(AppExtension)
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
            project.tasks.getByName("mergeDexRelease").doLast(new Action<Task>() {
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
                        throw new RuntimeException("BlackObfuscator not support the gradle version")
                    }
                    ObfDex.obf(finalFile.getAbsolutePath(),
                            sObfuscatorExtension.depth, sObfuscatorExtension.obfClass)
                }
            })

            project.tasks.getByName("mergeProjectDexDebug").doLast(new Action<Task>() {
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
                        throw new RuntimeException("BlackObfuscator not support the gradle version")
                    }
                    ObfDex.obf(finalFile.getAbsolutePath(),
                            sObfuscatorExtension.depth, sObfuscatorExtension.obfClass)
                }
            })
        }
        //注册一个Transform
//        def classTransform = new ObfTransform(project)
//        android.registerTransform(classTransform)
    }


}