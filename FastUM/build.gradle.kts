plugins {
    id("java")
    application
}

group = "dss.uminho"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.11.0")
    implementation("com.mysql:mysql-connector-j:8.3.0")
}

tasks.named<JavaExec>("run"){
    standardInput = System.`in`
    mainClass = "dss.uminho.Main"
}

tasks.test {
    useJUnitPlatform()
}