import java.io.FileInputStream
import java.util.Properties
import org.jetbrains.kotlin.config.KotlinCompilerVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    id("com.android.library")
    id("org.jlleitschuh.gradle.ktlint") version "9.0.0"
    kotlin("android")
    kotlin("android.extensions")
}

group = "com.sherepenko.android"

val archivesBaseName = "archivarius"
val version = BuildVersion(
    major = 1,
    minor = 0,
    patch = 0
)
val keystorePropertiesFile = rootProject.file("keystore.properties")

android {
    compileSdkVersion(29)

    defaultConfig {
        minSdkVersion(19)
        targetSdkVersion(29)
        versionCode = version.versionCode
        versionName = version.versionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        setProperty("archivesBaseName", "$archivesBaseName-$versionName")
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    lintOptions {
        ignore("InvalidPackage")
    }

    testOptions {
        unitTests.apply {
            isIncludeAndroidResources = true
        }
    }

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            val keystoreProperties = Properties().apply {
                load(FileInputStream(keystorePropertiesFile))
            }

            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("keystore.file"))
                storePassword = keystoreProperties.getProperty("keystore.password")
                keyAlias = keystoreProperties.getProperty("keystore.key.alias")
                keyPassword = keystoreProperties.getProperty("keystore.key.password")
            }
        }
    }

    buildTypes {
        getByName("release") {
            isZipAlignEnabled = true
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

ktlint {
    verbose.set(true)
    android.set(true)

    reporters {
        reporter(ReporterType.PLAIN)
        reporter(ReporterType.CHECKSTYLE)
    }
}

val okHttpVersion = "4.3.1"
val rxJavaVersion = "2.2.17"
val workVersion = "2.2.0"

dependencies {
    api("androidx.work:work-runtime-ktx:$workVersion")
    api("androidx.work:work-rxjava2:$workVersion")
    api("com.amazonaws:aws-android-sdk-s3:2.16.6")
    api("com.squareup.okhttp3:okhttp:$okHttpVersion")
    api("io.reactivex.rxjava2:rxjava:$rxJavaVersion")
    implementation(kotlin("stdlib-jdk8", KotlinCompilerVersion.VERSION))
    implementation("com.squareup.okhttp3:logging-interceptor:$okHttpVersion")
    testImplementation("junit:junit:4.12")
    testImplementation("androidx.test:core:1.2.0")
    testImplementation("androidx.test:runner:1.2.0")
    testImplementation("androidx.test.ext:junit:1.1.1")
    testImplementation("androidx.work:work-testing:$workVersion")
    testImplementation("com.google.truth:truth:0.44")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:$okHttpVersion")
    testImplementation("org.robolectric:robolectric:4.3.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
