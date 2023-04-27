plugins {
    id("java")
}

group = "exqudens"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.26")
    annotationProcessor("org.projectlombok:lombok:1.18.26")
    implementation("org.slf4j:slf4j-simple:2.0.7")
    implementation("org.apache.velocity.tools:velocity-tools-generic:3.1")
    implementation("org.dishevelled:dsh-commandline:1.2")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks {
    jar {
        manifest.attributes["Main-Class"] = "exqudens.java.velocity.Main"
        val dependencies = configurations
                .runtimeClasspath
                .get()
                .map(::zipTree) // OR .map { zipTree(it) }
        from(dependencies)
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
    test {
        useJUnitPlatform()
    }
}
