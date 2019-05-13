plugins {
    // Apply the java plugin to add support for Java
    java

    // Apply the application plugin to add support for building an application
    application

    // Support for generating parsers using ANTLR.
    antlr

    // Groovy support needed for Spock
    groovy
}

repositories {
    jcenter()
}

dependencies {
    antlr("org.antlr:antlr4:4.7.2")
    implementation("commons-cli:commons-cli:1.4")

    // Use the latest Groovy version for Spock testing
    testImplementation("org.codehaus.groovy:groovy-all:2.5.6")

    // Use the awesome Spock testing and specification framework even with Java
    testImplementation("org.spockframework:spock-core:1.2-groovy-2.5")
    testImplementation("junit:junit:4.12")
}

application {
    // Define the main class for the application
    mainClassName = "me.albmoriconi.mal.Assembler"
}

tasks.generateGrammarSource {
    arguments = arguments + listOf("-package", "me.albmoriconi.mal.antlr")
}

tasks.startScripts {
    doLast {
        unixScript.writeText(unixScript.readText().
                replaceFirst("cd \"\$(dirname \"\$0\")\"", "cd \"\$SAVED\""))
    }
}
