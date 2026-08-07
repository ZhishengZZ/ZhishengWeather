import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// 和风天气凭据（不入库，仅存 local.properties）
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.canRead()) load(f.inputStream())
}
fun lp(key: String, def: String = ""): String = localProps.getProperty(key, def)

android {
    namespace = "com.zhisheng.weather"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.zhisheng.weather"
        minSdk = 26
        targetSdk = 34
        versionCode = 10204
        versionName = "1.2.4"

        buildConfigField("String", "QW_HOST", "\"${lp("qw.host")}\"")
        buildConfigField("String", "QW_PROJECT_ID", "\"${lp("qw.project_id")}\"")
        buildConfigField("String", "QW_KID", "\"${lp("qw.kid", "TMWDYM6VB4")}\"")
        buildConfigField("String", "QW_PRIVATE_KEY", "\"${lp("qw.private_key")}\"")
    }

    signingConfigs {
        create("release") {
            val props = Properties()
            val f = rootProject.file("local.properties")
            if (f.canRead()) props.load(f.inputStream())
            storeFile = project.rootProject.file("keystore/zhisheng.jks")
            storePassword = props.getProperty("keystore.store_password") ?: "zhisheng123"
            keyAlias = "zhisheng"
            keyPassword = props.getProperty("keystore.key_password") ?: "zhisheng123"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.foundation)
    implementation(libs.compose.tooling.preview)
    implementation(libs.core.ktx)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.datastore.preferences)
    implementation(libs.coroutines.android)
    implementation(libs.bouncycastle)
    debugImplementation(libs.compose.tooling)
}
