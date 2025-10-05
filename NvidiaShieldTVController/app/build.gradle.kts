import org.gradle.kotlin.dsl.support.kotlinCompilerOptions
import java.io.FileNotFoundException

plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    defaultConfig {
        applicationId = "com.ashbreeze.shield_tv_controller"
        compileSdk = 36
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "1.0"

        // whether to log keypresses to logcat for mapping new controller buttons
        buildConfigField("boolean", "LOG_INPUT_KEYCODES", "false")
        buildConfigField("boolean", "IS_SHIELD_PRODUCT_VARIANT", "false")
        buildConfigField("String", "NETFLIX_BUTTON_APP_URI", "${project.property("NETFLIX_BUTTON_APP_URI")}")
        buildConfigField("String", "HA_BASE_URL", "${project.property("HOME_ASSISTANT_BASE_URL")}")
        buildConfigField("String", "HA_TOKEN", "\"${getHomeAssistantToken()}\"")

        buildConfigField("String", "BASE_PACKAGE_NAME", "\"${applicationId}\"")
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    buildTypes {
        named("release") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    flavorDimensions += "application_selection"
    productFlavors {
        // Shield product variant contains the accessibility service for using the netflix button
        // to return to the shield.

        for (productVariant in getProductVariantConfig()) {
            create(productVariant.name) {
                dimension = "application_selection"
                applicationIdSuffix = ".${productVariant.name}"
                versionNameSuffix = "-${productVariant.name}"
                manifestPlaceholders.putAll(mapOf(
                    "appPrefix" to productVariant.name,
                    "launcherName" to productVariant.launcherName,
                ))
                buildConfigField("String", "HA_COMMAND", "\"${productVariant.name}\"")
                if (productVariant.name == "shield") {
                    buildConfigField("boolean", "IS_SHIELD_PRODUCT_VARIANT", "true")
                }
            }
        }
    }
    namespace = "com.ashbreeze.shield_tv_controller"
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("androidx.core:core:1.17.0")
}

fun getHomeAssistantToken(): String {
    try {
        val lines = File("${project.rootDir}/home_assistant_token").readLines()
        return lines[0]
    } catch (ignored: FileNotFoundException) {
        println("*** Missing home_assistant.token file ****")
    }

    return ""
}

open class ProductVariant(
    val name: String,
    val launcherName: String,
)

fun getProductVariantConfig(): List<ProductVariant> {
    val lines = File("${project.rootDir}/product_variant_config.csv").readLines()
    val productVariants = mutableListOf<ProductVariant>()
    for (line in lines) {
        if (line.startsWith('#')) {
            continue
        }
        val parts = line.split(",")
        productVariants.add(ProductVariant(parts[0], parts[1]))
    }
    return productVariants
}