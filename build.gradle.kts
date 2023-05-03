plugins {
    id("java")
}

group = "ee.ut.cs.sep.openxescli"
version = "1.0"

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