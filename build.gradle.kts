plugins {
    // Apply the java plugin to add support for Java
    java

    // Apply the application plugin to add support for building an application
    application

    // Support for generating parsers using ANTLR.
    antlr
}

repositories {
    jcenter()
}

dependencies {
    antlr("org.antlr:antlr4:4.7.2")
    implementation("commons-cli:commons-cli:1.4")
}

application {
    // Define the main class for the application
    mainClassName = "it.albmoriconi.mal.Assembler"
}

tasks.generateGrammarSource {
    arguments = arguments + listOf("-package", "it.albmoriconi.mal.antlr")
}
