// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.android.library") version "8.2.2" apply false
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0" apply true
}

apply(from = "$rootDir/maven/config.gradle")