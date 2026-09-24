import java.util.zip.ZipFile
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.io.File

plugins {
    id("com.android.application")
}

android {
    namespace = "com.protostream.devkit"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.protostream.devkit"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
}

tasks.register("buildProtoGame") {
    group = "protostream"
    description = "Compiles the game and packages it into a .protogame file for ProtoStream."

    dependsOn("assembleRelease")

    // 1. Capture required paths during the Configuration Phase to avoid cache errors!
    val projDir = project.projectDir
    val buildDir = layout.buildDirectory.get().asFile

    doLast {
        // 2. Define the helper function inside doLast so it doesn't reference the outer script
        fun escapeJson(text: String): String {
            return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
        }

        // Use the paths captured above
        val javaSrcDir = File(projDir, "src/main/java")
        val outputDir = File(projDir, "output")
        if (!outputDir.exists()) outputDir.mkdirs()

        var detectedTitle = "Custom Game"
        var detectedAuthor = "Unknown Author"
        var detectedDesc = "Built with ProtoStream Dev Kit"
        var detectedMainClass = ""

        javaSrcDir.walkTopDown().filter { it.extension == "java" }.forEach { file ->
            val content = file.readText()
            if (content.contains("@GameInfo")) {
                var pkg = ""
                val pkgMatch = Regex("""package\s+([\w\.]+);""").find(content)
                if (pkgMatch != null) {
                    pkg = pkgMatch.groupValues[1]
                }
                val classMatch = Regex("""public\s+class\s+(\w+)\s+extends\s+ProtoGame""").find(content)
                if (classMatch != null) {
                    val className = classMatch.groupValues[1]
                    detectedMainClass = if (pkg.isNotEmpty()) "$pkg.$className" else className
                }

                val titleMatch = Regex("""title\s*=\s*"([^"]+)"""").find(content)
                if (titleMatch != null) detectedTitle = titleMatch.groupValues[1]

                val authorMatch = Regex("""author\s*=\s*"([^"]+)"""").find(content)
                if (authorMatch != null) detectedAuthor = authorMatch.groupValues[1]

                val descMatch = Regex("""description\s*=\s*"([^"]+)"""").find(content)
                if (descMatch != null) detectedDesc = descMatch.groupValues[1]
            }
        }

        if (detectedMainClass.isEmpty()) {
            throw GradleException("Could not find a class extending ProtoGame with @GameInfo annotation!")
        }

        println("=========================================")
        println("ProtoStream Dev Kit - Packaging Game")
        println("Title:      $detectedTitle")
        println("Author:     $detectedAuthor")
        println("Main Class: $detectedMainClass")
        println("=========================================")

        val metadataFile = File(buildDir, "tmp/metadata.json")
        metadataFile.parentFile.mkdirs()

        val jsonContent = """
            {
              "title": "${escapeJson(detectedTitle)}",
              "author": "${escapeJson(detectedAuthor)}",
              "description": "${escapeJson(detectedDesc)}",
              "mainClass": "$detectedMainClass"
            }
        """.trimIndent()

        metadataFile.writeText(jsonContent)

        val apkFile = File(buildDir, "outputs/apk/release/app-release-unsigned.apk")
        val dexFile = File(buildDir, "tmp/classes.dex")

        if (!apkFile.exists()) {
            throw GradleException("Release APK missing! Build failed.")
        }

        ZipFile(apkFile).use { zip ->
            val entry = zip.getEntry("classes.dex")
                ?: throw GradleException("classes.dex not found inside release APK!")
            zip.getInputStream(entry).use { input ->
                dexFile.outputStream().use { output -> input.copyTo(output) }
            }
        }

        val sanitizedFileName = detectedTitle.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val protogameFile = File(outputDir, "$sanitizedFileName.protogame")

        ZipOutputStream(protogameFile.outputStream()).use { zipOut ->
            zipOut.putNextEntry(ZipEntry("classes.dex"))
            dexFile.inputStream().use { it.copyTo(zipOut) }
            zipOut.closeEntry()

            zipOut.putNextEntry(ZipEntry("metadata.json"))
            metadataFile.inputStream().use { it.copyTo(zipOut) }
            zipOut.closeEntry()
        }

        println("SUCCESS: Generated .protogame file at:")
        println(" -> ${protogameFile.absolutePath}")
        println("=========================================")
    }
}