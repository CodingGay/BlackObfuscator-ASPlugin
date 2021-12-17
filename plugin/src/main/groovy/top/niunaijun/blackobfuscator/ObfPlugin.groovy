package top.niunaijun.blackobfuscator

import org.gradle.api.Plugin
import org.gradle.api.Project

import com.android.build.gradle.AppExtension

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

        //注册一个Transform
        def classTransform = new ObfTransform(project)
        android.registerTransform(classTransform)
    }
}