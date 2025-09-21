import com.diffplug.spotless.LineEnding

plugins {
    `java-library`
    `maven-publish`
    id("com.diffplug.spotless") version "7.2.1"
}

group = "com.github.malczuuu"

if (version == "unspecified") {
    version = Versioning.getSnapshotVersion(rootProject.rootDir)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io/") }
}

val jakartaServletVersion = "6.1.0"
val jakartaValidationVersion = "3.1.1"
val junitJupiterVersion = "5.13.4"
val junitPlatformVersion = "1.13.4"
val problem4jCoreVersion = "3.2.0-rc1"
val problem4jJacksonVersion = "3.2.0-rc1"
val slf4jVersion = "2.0.17"
val springBootVersion = "3.5.6"
val springFrameworkVersion = "6.2.11"

dependencies {
    api("org.springframework.boot:spring-boot-autoconfigure:${springBootVersion}")
    api("org.springframework:spring-webmvc:${springFrameworkVersion}")
    api("org.slf4j:slf4j-api:${slf4jVersion}")

    api("com.github.malczuuu:problem4j-core:${problem4jCoreVersion}")
    api("com.github.malczuuu:problem4j-jackson:${problem4jJacksonVersion}")

    compileOnly("jakarta.servlet:jakarta.servlet-api:${jakartaServletVersion}")
    compileOnly("jakarta.validation:jakarta.validation-api:${jakartaValidationVersion}")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:${springBootVersion}")

    testImplementation("jakarta.servlet:jakarta.servlet-api:${jakartaServletVersion}")
    testImplementation("jakarta.validation:jakarta.validation-api:${jakartaValidationVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitJupiterVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitJupiterVersion}")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:${junitPlatformVersion}")

    testImplementation("org.springframework.boot:spring-boot-starter-test:${springBootVersion}")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            from(components["java"])

            pom {
                name.set(project.name)
                description.set("Spring Web MVC integration for library implementing RFC7807")
                url.set("https://github.com/malczuuu/${project.name}")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
            }
        }
    }
}

spotless {
    format("misc") {
        target("**/*.gradle.kts", "**/.gitattributes", "**/.gitignore")

        trimTrailingWhitespace()
        leadingTabsToSpaces(4)
        endWithNewline()
        lineEndings = LineEnding.UNIX
    }

    java {
        target("src/**/*.java")

        googleJavaFormat("1.28.0")
        lineEndings = LineEnding.UNIX
    }
}

tasks.register("printVersion") {
    doLast {
        println("Project version: $version")
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
}

/**
 * Disable doclint to avoid errors and warnings on missing JavaDoc comments.
 */
tasks.withType<Javadoc>().configureEach {
    (options as StandardJavadocDocletOptions).apply {
        addStringOption("Xdoclint:none", "-quiet")
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
