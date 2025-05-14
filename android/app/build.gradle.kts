import com.github.jk1.license.render.SimpleHtmlReportRenderer
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

val localProps = Properties().apply {
    val propsFile = rootProject.file("local.properties")
    if (propsFile.exists()) {
        load(propsFile.inputStream())
    }
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
    alias(libs.plugins.detekt)
    alias(libs.plugins.dependency.license.report)
}

android {
    namespace = "com.aarokoinsaari.inwheel"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.aarokoinsaari.inwheel"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        val properties = Properties().apply {
            val localPropsFile = rootProject.file("local.properties")
            if (localPropsFile.exists()) {
                load(localPropsFile.inputStream())
            }
        }
        
        val apiKey = properties.getProperty("MAPS_API_KEY") ?: ""
        resValue("string", "maps_api_key", apiKey)
    }

    signingConfigs {
        create("release") {
            storeFile = rootProject.file(localProps["INWHEEL_KEYSTORE_FILE"] as String)
            storePassword = localProps["INWHEEL_KEYSTORE_PASSWORD"] as String
            keyAlias = localProps["INWHEEL_KEY_ALIAS"] as String
            keyPassword = localProps["INWHEEL_KEY_PASSWORD"] as String
        }
    }


    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("Boolean", "DEBUG_MODE", "false")
        }
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules-debug.pro"
            )
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
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    debugImplementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.config)
    implementation(libs.maps.compose)
    implementation(libs.maps.compose.utils)
    implementation(libs.maps.utils)
    implementation(libs.maps.utils.ktx)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.ktor.client.android)
    implementation(libs.okhttp.logging.interceptor)
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.postgrest)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.accompanist.permissions)
}

detekt {
    toolVersion = libs.versions.detekt.get()
    buildUponDefaultConfig = true
}

licenseReport {
    outputDir = "$projectDir/src/main/assets"
    renderers = arrayOf(SimpleHtmlReportRenderer("licenses.html"))
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    jvmTarget = "17"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}
