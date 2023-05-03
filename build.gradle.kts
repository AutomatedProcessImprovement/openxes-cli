plugins {
    application
    id("java")
}

group = "ee.ut.cs.sep.openxescli"
version = "1.0"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(files("lib/OpenXES-20211004.jar", "lib/Spex.jar"))
    implementation("com.google.guava:guava:31.0.1-jre")
    implementation("javax.xml.bind:jaxb-api:2.3.1")
    implementation("org.apache.commons:commons-csv:1.10.0")
    implementation("commons-cli:commons-cli:1.5.0")

    compileOnly("org.projectlombok:lombok:1.18.26")
    annotationProcessor("org.projectlombok:lombok:1.18.26")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("ee.ut.cs.sep.openxescli.Main")
}

tasks {
    jar {
        manifest {
            attributes(
                    "Main-Class" to "ee.ut.cs.sep.openxescli.Main"
            )
        }

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
        archiveFileName.set("openxes-cli.jar")
    }

}
