import java.io.FileInputStream
import java.util.Properties
import org.jetbrains.kotlin.config.KotlinCompilerVersion
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    id("com.android.application")
    id("com.sherepenko.gradle.plugin-build-version") version "0.2.3"
    id("org.jlleitschuh.gradle.ktlint") version "10.0.0"
    kotlin("android")
    kotlin("android.extensions")
}

val archivesBaseName = "s3-json-logger"

val keystorePropertiesFile = rootProject.file("keystore.properties")
val awsPropertiesFile = project.file("aws.properties")

android {
    compileSdkVersion(30)

    defaultConfig {
        minSdkVersion(19)
        targetSdkVersion(30)
        applicationId = "com.sherepenko.android.logger.example"
        versionCode = buildVersion.versionCode
        versionName = buildVersion.versionName
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        setProperty("archivesBaseName", "$archivesBaseName-$versionName")

        if (awsPropertiesFile.exists()) {
            val awsProperties = Properties().apply {
                load(FileInputStream(awsPropertiesFile))
            }

            buildConfigField(
                "String",
                "AWS_ACCESS_KEY_ENCODED",
                "\"${awsProperties.getProperty("aws.accessKeyEncoded")}\""
            )
            buildConfigField(
                "String",
                "AWS_SECRET_KEY_ENCODED",
                "\"${awsProperties.getProperty("aws.secretKeyEncoded")}\""
            )
            buildConfigField(
                "String",
                "AWS_BUCKET_NAME",
                "\"${awsProperties.getProperty("aws.bucketName")}\""
            )
            buildConfigField(
                "String",
                "AWS_BUCKET_REGION",
                "\"${awsProperties.getProperty("aws.bucketRegion")}\""
            )
        } else {
            buildConfigField("String", "AWS_ACCESS_KEY_ENCODED", "\"\"")
            buildConfigField("String", "AWS_SECRET_KEY_ENCODED", "\"\"")
            buildConfigField("String", "AWS_BUCKET_NAME", "\"\"")
            buildConfigField("String", "AWS_BUCKET_REGION", "\"\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
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
        create("release") {
            if (keystorePropertiesFile.exists()) {
                val keystoreProperties = Properties().apply {
                    load(FileInputStream(keystorePropertiesFile))
                }

                storeFile = rootProject.file(keystoreProperties.getProperty("keystore.file"))
                storePassword = keystoreProperties.getProperty("keystore.password")
                keyAlias = keystoreProperties.getProperty("keystore.key.alias")
                keyPassword = keystoreProperties.getProperty("keystore.key.password")
            } else {
                val debugSigningConfig = getByName("debug")

                storeFile = debugSigningConfig.storeFile
                storePassword = debugSigningConfig.storePassword
                keyAlias = debugSigningConfig.keyAlias
                keyPassword = debugSigningConfig.keyPassword
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
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

val loggerVersion = "1.0.3"
val kodeinVersion = "7.5.0"
val rxAndroidVersion = "2.1.1"
val rxKotlinVersion = "2.4.0"

dependencies {
    implementation(kotlin("stdlib-jdk8", KotlinCompilerVersion.VERSION))
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.core:core-ktx:1.3.2")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation("com.github.asherepenko:archivarius-logger:$loggerVersion")
    implementation("com.google.firebase:firebase-analytics-ktx:18.0.2")
    implementation("com.google.firebase:firebase-crashlytics-ktx:17.4.1")
    implementation("io.reactivex.rxjava2:rxandroid:$rxAndroidVersion")
    implementation("io.reactivex.rxjava2:rxkotlin:$rxKotlinVersion")
    implementation("org.kodein.di:kodein-di-jvm:$kodeinVersion")
    implementation("org.kodein.di:kodein-di-framework-android-x:$kodeinVersion")
}

apply(plugin = "com.google.gms.google-services")
apply(plugin = "com.google.firebase.crashlytics")
