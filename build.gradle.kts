import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.springframework.boot") version "2.4.3"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.spring") version "1.6.10"
}

group = "com.xxuz.piclane"
version = "1.0.10"
java.sourceCompatibility = JavaVersion.VERSION_11

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // graphql-java-kickstart
    implementation("com.graphql-java-kickstart:graphql-spring-boot-starter:12.0.0")
    implementation("com.graphql-java-kickstart:graphql-java-tools:12.0.2")

    // spring-boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // avoid CVE-2021-37136
    implementation("com.fasterxml.jackson.core:jackson-core:2.13.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.2.2")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.13.2")

    // Cache
    runtimeOnly("com.github.ben-manes.caffeine:caffeine")

    // JDBC
    runtimeOnly("org.postgresql:postgresql:42.3.4")

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // TEST
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // graphql-java-kickstart
    testImplementation("com.graphql-java-kickstart:graphql-spring-boot-starter-test:12.0.0")

    // spring-boot
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }

    // junit5
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")

    // dbunit
    testImplementation("org.dbunit:dbunit:2.7.3") {
        exclude(group = "junit", module = "junit")
    }
    testImplementation("com.github.springtestdbunit:spring-test-dbunit:1.3.0")

    // assertj
    testImplementation("org.assertj:assertj-core:3.22.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<BootJar> {
    launchScript()
}

springBoot {
    buildInfo()
}

tasks.register("package") {
    group = "build"
    description = "Package the distribution for release."
    dependsOn("bootJar")
    doLast {
        ant.withGroovyBuilder {
            val zipBaseDir = "${buildDir}/package"
            val zipDir = "${zipBaseDir}/fal-plus-api-${project.version}"
            val artifactPath = "${buildDir}/fal-plus-api-${project.version}-linux-amd64.tar.gz"

            // zipBaseDir をクリア
            "delete"("dir" to zipBaseDir, "quiet" to true)
            "mkdir"("dir" to zipDir)

            // 最終成果物をクリア
            "delete"("file" to artifactPath, "quiet" to true)

            // jar をコピー
            val jarPath = tasks.getByName("bootJar", org.springframework.boot.gradle.tasks.bundling.BootJar::class)
                .archiveFile
                .get()
                .asFile
            "copy" ("file" to jarPath, "todir" to zipDir)

            // install.sh / uninstall.sh をコピー
            "copy" ("file" to "${projectDir}/installer/install.sh", "todir" to zipDir)
            "copy" ("file" to "${projectDir}/installer/uninstall.sh", "todir" to zipDir)
            "exec" ("executable" to "chmod") {
                "arg"("value" to "+x")
                "arg"("value" to "${zipDir}/install.sh")
                "arg"("value" to "${zipDir}/uninstall.sh")
            }

            // README.md をコピー
            "copy" ("file" to "${projectDir}/README.md", "todir" to zipDir)

            // tar.gz で固める
            "exec" ("executable" to "tar", "dir" to zipBaseDir) {
                "arg"("value" to "-zcf")
                "arg"("value" to artifactPath)
                "arg"("value" to file(zipDir).name)
            }
        }
    }
}
