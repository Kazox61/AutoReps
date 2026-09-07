import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(projects.shared)

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)
}

compose.desktop {
    application {
        mainClass = "com.kazox.autoreps.MainKt"

        // TEMPORARY, alongside the demo seed: `./gradlew :desktopApp:run -Preseed`
        // wipes and regenerates the demo workouts.
        if (project.hasProperty("reseed")) {
            jvmArgs += "-Dautoreps.reseed=true"
        }

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.kazox.autoreps"
            packageVersion = "1.0.0"
        }
    }
}
