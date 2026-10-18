plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.example"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:4.2.0")
    annotationProcessor("com.velocitypowered:velocity-api:4.2.0")
    // YAML 設定ファイル読み込み用
    implementation("org.yaml:snakeyaml:2.7")
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

tasks.shadowJar {
    archiveClassifier.set("")
    minimize()
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
