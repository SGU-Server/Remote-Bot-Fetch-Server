plugins {
    `maven-publish`
    signing
    alias(libs.plugins.fabric.loom) apply false
    alias(libs.plugins.fabric.loom.remap) apply false

    // https://github.com/ReplayMod/preprocessor
    // https://github.com/Fallen-Breath/preprocessor
    alias(libs.plugins.preprocess)

    alias(libs.plugins.yamlang) apply false
    alias(libs.plugins.spotless)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.node.gradle)
}

repositories {
    mavenCentral()
}

val rootProjectRef: Project = project

preprocess {
    strictExtraMappings = false

    val mc1211 = createNode("1.21.1", 1_21_01, "")
    val mc1213 = createNode("1.21.3", 1_21_03, "")
    val mc1214 = createNode("1.21.4", 1_21_04, "")
    val mc1215 = createNode("1.21.5", 1_21_05, "")
    val mc1218 = createNode("1.21.8", 1_21_08, "")
    val mc12110 = createNode("1.21.10", 1_21_10, "")
    val mc12111 = createNode("1.21.11", 1_21_11, "")
    val mc260101 = createNode("26.1.2", 26_01_01, "")
    val mc260200 = createNode("26.2", 26_02_00, "")

    mc1211.link(mc1213)
    mc1213.link(mc1214)
    mc1214.link(mc1215)
    mc1215.link(mc1218)
    mc1218.link(mc12110)
    mc12110.link(mc12111, file("mappings/mapping-1.21.10-1.21.11.txt"))
    mc12111.link(mc260101)
    mc260101.link(mc260200)

    // See https://github.com/Fallen-Breath/fabric-mod-template/blob/1d72d77a1c5ce0bf060c2501270298a12adab679/build.gradle#L55-L63
    for (node in getNodes()) {
        val nodeProject =
            requireNotNull(rootProjectRef.findProject(node.project)) {
                "Project ${node.project} not found"
            }
        nodeProject.extensions.extraProperties["mcVersion"] = node.mcVersion
    }
}

tasks.register("buildAndGather") {
    group = "build"
    description = "Builds all subprojects and gathers their output jars into the root build/libs directory."
    dependsOn(project.subprojects.map { it.tasks.named("build") })
    doFirst {
        println("Gathering builds")
        val buildLibs: (Project) -> File = { p ->
            p.layout.buildDirectory
                .dir("libs")
                .get()
                .asFile
        }
        project.delete(project.fileTree(buildLibs(rootProject)) { include("*") })
        project.subprojects.forEach { subproject ->
            project.copy {
                from(buildLibs(subproject)) {
                    include("*.jar")
                    exclude("*-dev.jar", "*-sources.jar", "*-shadow.jar", "*-javadoc.jar")
                }
                into(buildLibs(rootProject))
                duplicatesStrategy = DuplicatesStrategy.INCLUDE
            }
        }
    }
}

spotless {
    val licenseHeaderFile = rootProject.file("copyright.txt")
    kotlinGradle {
        target("*.gradle.kts")
        ktlint(libs.versions.ktlint.get())
    }
    java {
        target("src/main/java/**/*.java", "versions/*/src/main/java/**/*.java")
        toggleOffOn()
        removeUnusedImports()
        forbidWildcardImports()
        forbidModuleImports()
        importOrderFile(rootProject.file("eclipse-importorder.txt"))
        cleanthat()
        val eclipseRelease = libs.versions.eclipse.get()
        val eclipseVersion = eclipseRelease.removePrefix("R-").substringBeforeLast("-")
        eclipse(eclipseVersion)
            .withP2Mirrors(
                mapOf(
                    "https://download.eclipse.org/eclipse/updates/$eclipseVersion/" to
                        "https://download.eclipse.org/eclipse/updates/$eclipseVersion/$eclipseRelease/",
                ),
            ).configFile(rootProject.file("eclipse-formatter.xml"))
        licenseHeaderFile(licenseHeaderFile)
    }
    format("styling") {
        target(
            "gradle/libs.versions.toml",
            "*.md",
            "*.json",
            "*.yml",
            "*.xml",
            "src/main/resources/*.json",
            "src/main/resources/assets/**/*.yml",
            "src/main/resources/assets/**/*.json",
            ".github/**/*.yml",
        )

        prettier(
            mapOf(
                "prettier" to libs.versions.prettier.get(),
                "prettier-plugin-toml" to
                    libs.versions.prettierPlugin.toml
                        .get(),
                "@prettier/plugin-xml" to
                    libs.versions.prettierPlugin.xml
                        .get(),
            ),
        ).config(
            mapOf(
                "plugins" to
                    listOf(
                        "prettier-plugin-toml",
                        "@prettier/plugin-xml",
                    ),
            ),
        )
    }
    format("text") {
        target(
            "LICENSE",
            "gradle.properties",
            "gradle/wrapper/gradle-wrapper.properties",
            "versions/*/gradle.properties",
            "versions/*/remote-bot-fetch-server.accesswidener",
            "copyright.txt",
            "mappings/*.txt",
            "eclipse-importorder.txt",
        )
        trimTrailingWhitespace()
        endWithNewline()
    }
}
