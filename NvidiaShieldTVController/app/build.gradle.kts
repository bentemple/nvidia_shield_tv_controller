import java.io.FileNotFoundException

plugins {
    id("com.android.application")
    id("kotlin-android")
    kotlin("plugin.serialization") version "1.9.20"
}

android {
    defaultConfig {
        applicationId = "com.ashbreeze.shield_tv_controller"
        compileSdk = 34
        minSdk = 26
        //noinspection ExpiredTargetSdkVersion Max SDK Version for Nvidia Shield is 28.
        targetSdk = 28
        versionCode = 1
        versionName = "1.0"

        // whether to log keypresses to logcat for mapping new controller buttons
        buildConfigField("boolean", "LOG_INPUT_KEYCODES", "false")
        buildConfigField("boolean", "IS_SHIELD_PRODUCT_VARIANT", "false")
        buildConfigField("String", "NETFLIX_BUTTON_APP_URI", "${project.property("NETFLIX_BUTTON_APP_URI")}")
        buildConfigField("boolean", "USE_HOME_ASSISTANT", "${project.property("USE_HOME_ASSISTANT")}")

        buildConfigField(
            "boolean",
            "USE_HOME_ASSISTANT",
            "${project.property("USE_HOME_ASSISTANT")}"
        )
        buildConfigField("String", "HA_BASE_URL", "${project.property("HOME_ASSISTANT_BASE_URL")}")
        buildConfigField("String", "HA_TOKEN", "\"${getHomeAssistantToken()}\"")
        // If not using Home Assistant, will use this IP / Port and directly send telnet command to select
        // the given input.
        buildConfigField(
            "String",
            "TV_IP_ADDRESS",
            "${project.property("TV_DIRECT_ACCESS_ADDRESS")}"
        )
        buildConfigField("int", "TV_PORT", "${project.property("TV_DIRECT_ACCESS_PORT")}")
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

    kotlinOptions {
        jvmTarget = "17"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
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
                buildConfigField("String", "SELECT_INPUT_PARAMS", "\"${productVariant.selectedInput}\"")
                if (productVariant.name == "shield") {
                    buildConfigField("boolean", "IS_SHIELD_PRODUCT_VARIANT", "true")
                }
            }
        }
    }
    namespace = "com.ashbreeze.shield_tv_controller"
}

val ktorVersion = "2.3.12"
dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${property("kotlin_version")}")
    implementation("androidx.leanback:leanback:1.0.0")

    implementation("commons-net:commons-net:3.6")
    implementation("commons-io:commons-io:2.13.0")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-auth:$ktorVersion")
    implementation("io.ktor:ktor-client-android:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("org.slf4j:slf4j-android:1.7.25")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
}

fun getHomeAssistantToken(): String {
    if (project.property("USE_HOME_ASSISTANT") != "true") {
        return ""
    }

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
    val selectedInput: String = "",
    val command: String = name,
)

fun getProductVariantConfig(): List<ProductVariant> {
    val lines = File("${project.rootDir}/product_variant_config.csv").readLines()
    val productVariants = mutableListOf<ProductVariant>()
    for (line in lines) {
        if (line.startsWith('#')) {
            continue
        }
        val parts = line.split(",")
        productVariants.add(ProductVariant(parts[0], parts[1], parts[2]))
    }
    return productVariants
}