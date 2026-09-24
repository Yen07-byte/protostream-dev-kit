plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
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

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    // Basic Android dependencies required for building DEX files
    implementation("androidx.core:core-ktx:1.12.0")
}

/**
 * Custom Gradle Task: buildProtoGame
 * 
 * This task automates the entire .protogame creation pipeline:
 * 1. Runs standard release build to compile code into Android DEX bytecode (classes.dex).
 * 2. Parses the primary Java source file to extract @GameInfo metadata (title, author, desc) & mainClass.
 * 3. Generates the required metadata.json file.
 * 4. Zips classes.dex and metadata.json into a .protogame file in the output folder.
 */
tasks.register("buildProtoGame") {
    group = "protostream"
    description = "Compiles the game and packages it into a .protogame file for ProtoStream."

    // Depends on assembling the release build first so DEX is generated
    dependsOn("assembleRelease")

    doLast {
        val projectDir = project.projectDir
        val javaSrcDir = file("$projectDir/src/main/java")
        val outputDir = file("$projectDir/output")
        if (!outputDir.exists()) outputDir.mkdirs()

        var detectedTitle = "Custom Game"
        var detectedAuthor = "Unknown Author"
        var detectedDesc = "Built with ProtoStream Dev Kit"
        var detectedMainClass = ""

        // Scan java files for @GameInfo and package name
        javaSrcDir.walkTopDown().filter { it.extension == "java" }.forEach { file ->
            val content = file.readText()
            if (content.contains("@GameInfo")) {
                // Extract package and class name to resolve mainClass
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

                // Extract title
                val titleMatch = Regex("""title\s*=\s*"([^"]+)"""").find(content)
                if (titleMatch != null) detectedTitle = titleMatch.groupValues[1]

                // Extract author
                val authorMatch = Regex("""author\s*=\s*"([^"]+)"""").find(content)
                if (authorMatch != null) detectedAuthor = authorMatch.groupValues[1]

                // Extract description
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

        val metadataFile = file("$buildDir/tmp/metadata.json")
        metadataFile.parentFile.mkdirs()

        // Format JSON manually to avoid external heavy dependencies
        val jsonContent = """
            {
              "title": "${escapeJson(detectedTitle)}",
              "author": "${escapeJson(detectedAuthor)}",
              "description": "${escapeJson(detectedDesc)}",
              "mainClass": "$detectedMainClass"
            }
        """.trimIndent()

        metadataFile.writeText(jsonContent)

        // Search inside APK output for classes.dex
        val apkFile = file("$buildDir/outputs/apk/release/app-release-unsigned.apk")
        val dexFile = file("$buildDir/tmp/classes.dex")

        if (!apkFile.exists()) {
            throw GradleException("Release APK missing! Build failed.")
        }

        // Extract classes.dex from the assembled APK
        java.util.zip.ZipFile(apkFile).use { zip ->
            val entry = zip.getEntry("classes.dex")
                ?: throw GradleException("classes.dex not found inside release APK!")
            zip.getInputStream(entry).use { input ->
                dexFile.outputStream().use { output -> input.copyTo(output) }
            }
        }

        val sanitizedFileName = detectedTitle.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val protogameFile = file("$outputDir/$sanitizedFileName.protogame")

        java.util.zip.ZipOutputStream(protogameFile.outputStream()).use { zipOut ->
            // Add classes.dex
            zipOut.putNextEntry(java.util.zip.ZipEntry("classes.dex"))
            dexFile.inputStream().use { it.copyTo(zipOut) }
            zipOut.closeEntry()

            // Add metadata.json
            zipOut.putNextEntry(java.util.zip.ZipEntry("metadata.json"))
            metadataFile.inputStream().use { it.copyTo(zipOut) }
            zipOut.closeEntry()
        }

        println("SUCCESS: Generated .protogame file at:")
        println(" -> ${protogameFile.absolutePath}")
        println("=========================================")
    }
}

fun escapeJson(text: String): String {
    return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
}