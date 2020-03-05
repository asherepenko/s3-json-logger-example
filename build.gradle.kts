buildscript {
    repositories {
        jcenter()
        google()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:3.6.1")
        classpath ("com.google.gms:google-services:4.3.3")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.0.0-beta02")
        classpath(kotlin("gradle-plugin", version = "1.3.70"))
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        maven(url = "https://jitpack.io")
    }
}

val clean by tasks.registering(Delete::class) {
    delete(rootProject.buildDir)
}
