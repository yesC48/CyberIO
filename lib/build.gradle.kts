import io.github.liplum.mindustry.importMindustry
plugins {
    kotlin("jvm")
    `maven-publish`
    id("io.github.liplum.mgpp")
}

sourceSets {
    main {
        java.srcDir("src")
        resources.srcDir("resources")
    }
    test {
        java.srcDir("test")
        resources.srcDir("resources")
    }
}
val MKUtilsVersion :String by project
dependencies {
    importMindustry()
    api("com.github.plumygame.mkutils:core:$MKUtilsVersion")
    testApi("com.github.plumygame.mkutils:core:$MKUtilsVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
    testImplementation("com.github.liplum:TestUtils:v0.1")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}