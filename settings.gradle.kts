// 本地加速镜像开关：local.properties 声明 repo.mirror=aliyun 时启用（境内网络更快）；
// 未声明时（如 GitHub CI 海外 runner）走官方源，避免镜像 5xx 导致构建失败。
// 注意：pluginManagement 属 settings 早期求值阶段，编译作用域受限（不认顶层 import / 变量），
// 故两处块内各自用全限定名读取。

pluginManagement {
    repositories {
        val mirror = java.util.Properties().let { p ->
            val f = File("local.properties")
            if (f.canRead()) p.load(f.inputStream())
            p.getProperty("repo.mirror", "") == "aliyun"
        }
        if (mirror) {
            maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
            maven { url = uri("https://maven.aliyun.com/repository/google") }
            maven { url = uri("https://maven.aliyun.com/repository/public") }
        }
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        val mirror = java.util.Properties().let { p ->
            val f = File("local.properties")
            if (f.canRead()) p.load(f.inputStream())
            p.getProperty("repo.mirror", "") == "aliyun"
        }
        if (mirror) {
            maven { url = uri("https://maven.aliyun.com/repository/google") }
            maven { url = uri("https://maven.aliyun.com/repository/public") }
            maven { url = uri("https://maven.aliyun.com/repository/central") }
        }
        google()
        mavenCentral()
    }
}

rootProject.name = "ZhishengWeather"
include(":app")
