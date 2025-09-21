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

dependencies {
    api("org.springframework.boot:spring-boot-autoconfigure:3.5.6")
    api("org.springframework:spring-webmvc:6.2.10")
    api("org.slf4j:slf4j-api:2.0.17")

    api("com.github.malczuuu:problem4j-core:3.2.0-rc1")
    api("com.github.malczuuu:problem4j-jackson:3.2.0-rc1")

    compileOnly("jakarta.servlet:jakarta.servlet-api:6.1.0")
    compileOnly("jakarta.validation:jakarta.validation-api:3.1.1")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:3.5.6")

    testImplementation("jakarta.servlet:jakarta.servlet-api:6.1.0")
    testImplementation("jakarta.validation:jakarta.validation-api:3.1.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.13.4")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.13.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.13.4")

    testImplementation("org.springframework.boot:spring-boot-starter-test:3.5.6")
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
