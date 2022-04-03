package top.niunaijun.blackobfuscator
import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.internal.api.ReadOnlyProductFlavor
import org.gradle.api.*
import top.niunaijun.blackobfuscator.core.ObfDex
public class ObfPlugin implements Plugin<Project> {
    private String PLUGIN_NAME = "BlackObfuscator"
    private Project mProject
    public static BlackObfuscatorExtension sObfuscatorExtension
    public Map<String, String> mTaskMapping = new HashMap<>()

    void apply(Project project) {
        // 缓存project
        this.mProject = project
        // 获取android:AppExtension
        def android = project.extensions.findByType(AppExtension)
        // 自定义配置
        project.configurations.create(PLUGIN_NAME).extendsFrom(project.configurations.implementation)
        sObfuscatorExtension = project.extensions.create(PLUGIN_NAME, BlackObfuscatorExtension, project)
        // 清理任务映射
        mTaskMapping.clear()
        // 输出日志提示信息
        project.afterEvaluate {
            System.out.println("=====BlackObfuscator=====")
            System.out.println(sObfuscatorExtension.toString())
            System.out.println("=========================")
        }
        // 配置自定义任务
        project.afterEvaluate { ->
            if (!sObfuscatorExtension.enabled) {
                return
            }

            //初始化任务列表
            List<Task> tasks = new ArrayList<>()
            if (android != null) {
                android.applicationVariants.all(new Action<ApplicationVariant>() {
                    @Override
                    void execute(ApplicationVariant applicationVariant) {
                        File mappingFile = null
                        if (applicationVariant.buildType.minifyEnabled) {
                            mappingFile = applicationVariant.mappingFile
                        }

                        def buildType = upperCaseFirst(applicationVariant.buildType.name)

                        boolean empty = true
                        for (ReadOnlyProductFlavor flavor : applicationVariant.productFlavors) {
                            def flavorName = upperCaseFirst(applicationVariant.flavorName)
                            addOtherTask(tasks, flavorName, buildType, mappingFile)
                            empty = false
                            break
                        }
                        if (empty) {
                            addOtherTask(tasks, "", buildType, mappingFile)
                        }
                    }
                })
            }

            // 自定义action
            def action = new Action<Task>() {
                @Override
                void execute(Task task) {
                    task.getOutputs().getFiles().collect().each() { element ->
                        def file = new File(element.toString())
                        def taskOutputFilePath = file.getAbsolutePath()
                        def mappingFilePath = mTaskMapping.get(task.name)
                        // 混淆核心逻辑
                        ObfDex.obf(taskOutputFilePath,
                                sObfuscatorExtension.depth,
                                sObfuscatorExtension.obfClass,
                                sObfuscatorExtension.blackClass,
                                mappingFilePath)
                    }
                }
            }

            // 遍历任务列表 通过doLast 追加自定义action
            for (Task task : tasks) {
                task.doLast(action)
            }

            if (tasks.isEmpty()) {
                System.err.println("This gradle version is not applicable. Please submit issues in https://github.com/CodingGay/BlackObfuscator-ASPlugin")
            }
        }
    }

    private void addOtherTask(List<Task> tasks, String name, String buildType, File mappingFile) {
        addTask("mergeDex${name}${buildType}", tasks, mappingFile)
        addTask("mergeLibDex${name}${buildType}", tasks, mappingFile)
        addTask("mergeProjectDex${name}${buildType}", tasks, mappingFile)
        addTask("transformDexArchiveWithDexMergerFor${name}${buildType}", tasks, mappingFile)
        addTask("minify${name}${buildType}WithR8", tasks, mappingFile)
        println("$name$buildType mappingFile $mappingFile")
    }

    private String upperCaseFirst(String val) {
        char[] arr = val.toCharArray()
        arr[0] = Character.toUpperCase(arr[0])
        return new String(arr)
    }

    private void addTask(String name, List<Task> tasks, File mappingFile) {
        try {
            //Protected code
            Task task = mProject.tasks.getByName(name)
            if (!tasks.contains(task)) {
                tasks.add(task)
                if (mappingFile != null) {
                    mTaskMapping.put(task.name, mappingFile.absolutePath)
                }
                println("add Task $name")
            }
        } catch(UnknownTaskException e1) {
            //Catch block
        }
    }
}