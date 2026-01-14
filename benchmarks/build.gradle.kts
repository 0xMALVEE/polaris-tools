/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.nosphere.apache.rat.RatTask

plugins {
    scala
    id("io.gatling.gradle") version "3.13.5.2"
    id("com.diffplug.spotless") version "7.0.2"
    id("org.nosphere.apache.rat") version "0.8.1"
}

description = "Polaris Iceberg REST API performance tests"

tasks.withType<ScalaCompile> {
    scalaCompileOptions.forkOptions.apply {
        jvmArgs = listOf("-Xss100m") // Scala compiler may require a larger stack size when compiling Gatling simulations
    }
}

dependencies {
    gatling("com.typesafe.play:play-json_2.13:2.9.4")
    gatling("com.typesafe:config:1.4.3")
}

repositories {
    mavenCentral()
}

// Create a standalone distribution with all dependencies
val standaloneDistDir = layout.buildDirectory.dir("standalone")

tasks.register<Copy>("copyDependencies") {
    from(configurations.getByName("gatlingRuntimeClasspath"))
    into(standaloneDistDir.map { it.dir("lib") })
}

tasks.register<Copy>("copySimulations") {
    dependsOn("gatlingClasses")
    from(layout.buildDirectory.dir("classes/scala/gatling"))
    into(standaloneDistDir.map { it.dir("lib/classes") })
}

tasks.register<Copy>("copyResources") {
    from("src/gatling/resources")
    into(standaloneDistDir.map { it.dir("conf") })
}

tasks.register("standaloneDist") {
    dependsOn("copyDependencies", "copySimulations", "copyResources")
    group = "distribution"
    description = "Creates a standalone distribution with all dependencies"

    doLast {
        // Create runner script
        val scriptFile = standaloneDistDir.get().file("run-benchmark.sh").asFile
        scriptFile.writeText("""
#!/bin/bash
set -e

SCRIPT_DIR="${'$'}(cd "${'$'}(dirname "${'$'}0")" && pwd)"
LIB_DIR="${'$'}SCRIPT_DIR/lib"
CONF_DIR="${'$'}SCRIPT_DIR/conf"

# Find all JARs
CLASSPATH="${'$'}LIB_DIR/classes"
for jar in "${'$'}LIB_DIR"/*.jar; do
    CLASSPATH="${'$'}CLASSPATH:${'$'}jar"
done

# Default simulation
SIMULATION="${'$'}{1:-org.apache.polaris.benchmarks.simulations.CreateTreeDataset}"

# Config file (optional second argument)
CONFIG_OPTS=""
if [ -n "${'$'}2" ]; then
    CONFIG_OPTS="-Dconfig.file=${'$'}2"
elif [ -f "${'$'}CONF_DIR/application.conf" ]; then
    CONFIG_OPTS="-Dconfig.file=${'$'}CONF_DIR/application.conf"
fi

echo "Running simulation: ${'$'}SIMULATION"
echo "Classpath: ${'$'}CLASSPATH"

java -cp "${'$'}CLASSPATH" ${'$'}CONFIG_OPTS \
    -Dgatling.core.directory.results="${'$'}SCRIPT_DIR/results" \
    io.gatling.app.Gatling \
    --simulation "${'$'}SIMULATION" \
    --results-folder "${'$'}SCRIPT_DIR/results"
""".trimIndent())
        scriptFile.setExecutable(true)

        println("Standalone distribution created at: ${standaloneDistDir.get().asFile.absolutePath}")
        println("Run with: ./run-benchmark.sh [SimulationClass] [config-file]")
    }
}

// Create distributable tarball (requires Java on target machine)
tasks.register<Exec>("distTarball") {
    dependsOn("standaloneDist")
    group = "distribution"
    description = "Creates a tarball of the standalone distribution"

    val tarball = layout.buildDirectory.file("iceberg-rest-benchmark.tar.gz")

    commandLine("tar", "-czf", tarball.get().asFile.absolutePath,
        "-C", layout.buildDirectory.get().asFile.absolutePath, "standalone")

    doLast {
        println("Tarball created: ${tarball.get().asFile.absolutePath}")
    }
}

// Bundle JRE for fully standalone distribution (no Java required on target)
tasks.register("bundleJre") {
    dependsOn("standaloneDist")
    group = "distribution"
    description = "Downloads and bundles a JRE for fully standalone distribution"

    doLast {
        val jreDir = standaloneDistDir.get().dir("jre").asFile
        val jreTarball = layout.buildDirectory.file("jre.tar.gz").get().asFile

        // Detect platform
        val os = System.getProperty("os.name").lowercase()
        val arch = System.getProperty("os.arch").let {
            if (it == "amd64" || it == "x86_64") "x64" else it
        }
        val platform = when {
            os.contains("linux") -> "linux"
            os.contains("mac") -> "mac"
            os.contains("win") -> "windows"
            else -> "linux"
        }

        val url = "https://api.adoptium.net/v3/binary/latest/21/ga/$platform/$arch/jre/hotspot/normal/eclipse?project=jdk"

        println("Downloading JRE from Adoptium...")
        exec {
            commandLine("curl", "-L", "-o", jreTarball.absolutePath, url)
        }

        println("Extracting JRE...")
        jreDir.mkdirs()
        exec {
            commandLine("tar", "-xzf", jreTarball.absolutePath, "-C", jreDir.absolutePath, "--strip-components=1")
        }

        // Update run script to use bundled JRE
        val scriptFile = standaloneDistDir.get().file("run-benchmark.sh").asFile
        scriptFile.writeText("""
#!/bin/bash
set -e

SCRIPT_DIR="${'$'}(cd "${'$'}(dirname "${'$'}0")" && pwd)"
LIB_DIR="${'$'}SCRIPT_DIR/lib"
CONF_DIR="${'$'}SCRIPT_DIR/conf"
JAVA_CMD="${'$'}SCRIPT_DIR/jre/bin/java"

# Find all JARs
CLASSPATH="${'$'}LIB_DIR/classes"
for jar in "${'$'}LIB_DIR"/*.jar; do
    CLASSPATH="${'$'}CLASSPATH:${'$'}jar"
done

# Default simulation
SIMULATION="${'$'}{1:-org.apache.polaris.benchmarks.simulations.CreateTreeDataset}"

# Config file (optional second argument)
CONFIG_OPTS=""
if [ -n "${'$'}2" ]; then
    CONFIG_OPTS="-Dconfig.file=${'$'}2"
elif [ -f "${'$'}CONF_DIR/application.conf" ]; then
    CONFIG_OPTS="-Dconfig.file=${'$'}CONF_DIR/application.conf"
fi

echo "Running simulation: ${'$'}SIMULATION"

"${'$'}JAVA_CMD" -cp "${'$'}CLASSPATH" ${'$'}CONFIG_OPTS \
    -Dgatling.core.directory.results="${'$'}SCRIPT_DIR/results" \
    io.gatling.app.Gatling \
    --simulation "${'$'}SIMULATION" \
    --results-folder "${'$'}SCRIPT_DIR/results"
""".trimIndent())
        scriptFile.setExecutable(true)

        println("JRE bundled successfully!")
    }
}

// Create fully standalone tarball with bundled JRE
tasks.register<Exec>("distTarballWithJre") {
    dependsOn("bundleJre")
    group = "distribution"
    description = "Creates a tarball with bundled JRE (no Java required on target)"

    val tarball = layout.buildDirectory.file("iceberg-rest-benchmark-standalone.tar.gz")

    commandLine("tar", "-czf", tarball.get().asFile.absolutePath,
        "-C", layout.buildDirectory.get().asFile.absolutePath, "standalone")

    doLast {
        println("Standalone tarball created: ${tarball.get().asFile.absolutePath}")
    }
}

spotless {
    scala {
        // Use scalafmt for Scala formatting
        scalafmt("3.9.3").configFile(".scalafmt.conf")
        // Add license header to Scala files
        licenseHeaderFile(rootProject.file("codestyle/copyright-header-scala.txt"), "package ")
    }
}

tasks.named<RatTask>("rat").configure {
    // Gradle
    excludes.add("**/build/**")
    excludes.add("gradle/wrapper/gradle-wrapper*")
    excludes.add(".gradle")

    excludes.add("LICENSE")
    excludes.add("DISCLAIMER")
    excludes.add("NOTICE")

    // Git & GitHub
    excludes.add(".git")
    excludes.add(".github/pull_request_template.md")

    // Misc build artifacts
    excludes.add("**/.keep")
    excludes.add("logs/**")
    excludes.add("**/*.lock")

    // Configuration files that cannot have headers
    excludes.add("**/*.conf")  // Gatling and HOCON config files
    excludes.add("**/*.properties")  // Gradle wrapper properties

    // Binary files
    excludes.add("**/*.jar")
    excludes.add("**/*.zip")
    excludes.add("**/*.tar.gz")
    excludes.add("**/*.tgz")
    excludes.add("**/*.class")

    // IntelliJ
    excludes.add(".idea")
    excludes.add("**/*.iml")
    excludes.add("**/*.iws")

    // Rat can't scan binary images
    excludes.add("**/*.png")
    excludes.add("**/*.svg")
    excludes.add("**/*.puml")
}
