/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2019-2024 The TurnKey Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.diffplug.gradle.spotless.JavaExtension
import com.diffplug.gradle.spotless.SpotlessTask
import com.github.gradle.node.variant.computeNodeDir
import com.github.gradle.node.variant.computeNodeExec
import com.github.spotbugs.snom.Effort.MAX
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
  `java-library`
  `maven-publish`
  pmd
  signing

  alias(libs.plugins.gitVersioning)
  alias(libs.plugins.moduleInfo)
  alias(libs.plugins.nexusPublish)
  alias(libs.plugins.node)
  alias(libs.plugins.spotbugs)
  alias(libs.plugins.spotless)
  alias(libs.plugins.taskTree)
  alias(libs.plugins.versions)
}

group = "tools.aqua"

version = "0.0.0-undetected-SNAPSHOT"

gitVersioning.apply {
  describeTagFirstParent = false
  refs {
    considerTagsOnBranches = true
    tag("(?<version>.*)") {
      // on a tag: use the tag name as is
      version = "\${ref.version}"
    }
    branch("main") {
      // on the main branch: use <last.tag.version>-<distance>-<commit>-SNAPSHOT
      version = "\${describe.tag.version}-\${describe.distance}-\${commit.short}-SNAPSHOT"
    }
    branch(".+") {
      // on other branches: use <last.tag.version>-<branch.name>-<distance>-<commit>-SNAPSHOT
      version =
          "\${describe.tag.version}-\${ref.slug}-\${describe.distance}-\${commit.short}-SNAPSHOT"
    }
  }

  // optional fallback configuration in case of no matching ref configuration
  rev {
    // in case of missing git data: use 0.0.0-unknown-0-<commit>-SNAPSHOT
    version = "0.0.0-unknown-0-\${commit.short}-SNAPSHOT"
  }
}

repositories { mavenCentral() }

dependencies {
  api(libs.jspecify)

  testImplementation(platform(libs.junit))
  testImplementation(libs.assertj.core)
  testImplementation(libs.junit.jupiter)
  testRuntimeOnly(libs.junit.launcher)
}

node {
  download = true
  workDir = layout.buildDirectory.dir("nodejs")
}

fun isNonStable(version: String): Boolean {
  val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
  val regex = "^[0-9,.v-]+(-r)?$".toRegex()
  val isStable = stableKeyword || regex.matches(version)
  return isStable.not()
}

tasks.dependencyUpdates {
  gradleReleaseChannel = "current"
  revision = "release"
  rejectVersionIf { isNonStable(candidate.version) && !isNonStable(currentVersion) }
}

pmd {
  isConsoleOutput = true
  toolVersion = libs.pmd.get().version!!
  ruleSetFiles(
      resources.text.fromFile("config/pmd.xml"), resources.text.fromFile("config/pmd-main.xml"))
}

spotbugs { effort = MAX }

spotless {
  format("javaMain", JavaExtension::class.java) {
    target(sourceSets.main.get().java.filter { it.extension == "java" })
    licenseHeaderFile(project.file("config/license/ISC-cstyle")).updateYearWithLatest(true)
    googleJavaFormat()
  }
  format("javaTest", JavaExtension::class.java) {
    target(sourceSets.test.get().java.filter { it.extension == "java" })
    licenseHeaderFile(project.file("config/license/Apache-2.0-cstyle")).updateYearWithLatest(true)
    googleJavaFormat()
  }
  kotlinGradle {
    licenseHeaderFile(project.file("config/license/Apache-2.0-cstyle"), "(plugins|import )")
        .updateYearWithLatest(true)
    ktfmt()
  }
  format("markdown") {
    target("*.md")
    licenseHeaderFile(project.file("config/license/CC-BY-4.0-xmlstyle"), """(#+|\[!\[)""")
        .updateYearWithLatest(true)
    prettier()
        .npmInstallCache()
        .nodeExecutable(computeNodeExec(node, computeNodeDir(node)).get())
        .config(mapOf("parser" to "markdown", "printWidth" to 100, "proseWrap" to "always"))
  }
  yaml {
    target(".github/**/*.yml")
    licenseHeaderFile(project.file("config/license/Apache-2.0-hashmark"), "[A-Za-z-]+:")
        .updateYearWithLatest(true)
    prettier()
        .npmInstallCache()
        .nodeExecutable(computeNodeExec(node, computeNodeDir(node)).get())
        .config(mapOf("parser" to "yaml", "printWidth" to 100))
  }
  format("toml") {
    target("gradle/libs.versions.toml")
    licenseHeaderFile(project.file("config/license/Apache-2.0-hashmark"), """\[[A-Za-z-]+]""")
        .updateYearWithLatest(true)
    prettier(mapOf("prettier-plugin-toml" to libs.versions.prettier.toml.get()))
        .npmInstallCache()
        .nodeExecutable(computeNodeExec(node, computeNodeDir(node)).get())
        .config(
            mapOf(
                "plugins" to listOf("prettier-plugin-toml"),
                "parser" to "toml",
                "alignComments" to false,
                "printWidth" to 100,
            ))
  }
  format("xml") {
    target("config/**/*.xml", sourceSets.test.get().resources.filter { it.extension == "xml" })
    licenseHeaderFile(project.file("config/license/Apache-2.0-xmlstyle"), "<((!DOCTYPE)|[A-Za-z]+)")
        .skipLinesMatching("^<\\?xml.*\\?>$")
        .updateYearWithLatest(true)
    prettier(mapOf("@prettier/plugin-xml" to libs.versions.prettier.xml.get()))
        .npmInstallCache()
        .nodeExecutable(computeNodeExec(node, computeNodeDir(node)).get())
        .config(
            mapOf(
                "plugins" to listOf("@prettier/plugin-xml"),
                "parser" to "xml",
                "printWidth" to 100,
                "xmlQuoteAttributes" to "double",
                "xmlWhitespaceSensitivity" to "ignore"))
  }
}

tasks.withType<SpotlessTask>().configureEach { dependsOn(tasks.npmSetup) }

java {
  toolchain { languageVersion = JavaLanguageVersion.of(8) }
  modularity.inferModulePath = false
  withJavadocJar()
  withSourcesJar()
}

tasks.test {
  useJUnitPlatform()
  testLogging { events(PASSED, SKIPPED, FAILED) }
}

tasks.javadoc {
  // we are using a Java 8 toolchain, so javadoc does not know about modules
  exclude("module-info.java")
  (options as? StandardJavadocDocletOptions)?.links("https://docs.oracle.com/javase/8/docs/api/")
}

tasks.compileModuleInfo {
  moduleVersion = version.toString()
  targetFile = layout.buildDirectory.file("mic/META-INF/versions/9/module-info.class")
}

tasks.jar {
  from(layout.buildDirectory.dir("mic"))
  manifest { attributes("Multi-Release" to "True") }
}

val maven by
    publishing.publications.creating(MavenPublication::class) {
      from(components["java"])
      pom {
        name = "TurnKey Support Library"
        description =
            "Support code for TurnKey libraries that provides code for unpacking and loading " +
                "native libraries in a mostly platform-independent fashion."
        url = "https://github.com/tudo-aqua/turnkey-support"
        licenses {
          license {
            name = "ISC License"
            url = "https://opensource.org/licenses/ISC"
          }
        }
        developers {
          developer {
            name = "Simon Dierl"
            email = "simon.dierl@tu-dortmund.de"
            organization = "AQUA Group, Department of Computer Science, TU Dortmund University"
            organizationUrl = "https://aqua.engineering/"
          }
        }
        scm {
          connection = "scm:git:https://github.com/tudo-aqua/turnkey-support.git"
          developerConnection = "scm:git:ssh://git@github.com:tudo-aqua/turnkey-support.git"
          url = "https://github.com/tudo-aqua/turnkey-support/tree/main"
        }
      }
    }

signing {
  setRequired { gradle.taskGraph.allTasks.any { it is PublishToMavenRepository } }
  useGpgCmd()
  sign(maven)
}

nexusPublishing { this.repositories { sonatype() } }
