plugins {
    id("java")
    id("maven-publish")
}

group = "dev.lightdream"
version = "1.15.4"

repositories {
    mavenCentral()
    maven("https://repo.lightdream.dev/")
    maven("https://mvnrepository.com/artifact/redis.clients/jedis")
    maven("https://mvnrepository.com/artifact/org.jetbrains/annotations")
    maven("https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-api")
}

dependencies {
    // LightDream
    implementation("dev.lightdream:logger:3.2.0")
    implementation("dev.lightdream:lambda:4.0.0")

    // Lombok
    implementation("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.24")

    // Jedis
    implementation("redis.clients:jedis:4.4.0-m2")

    // JetBrains
    implementation("org.jetbrains:annotations:23.1.0")

    // Reflections
    implementation("org.reflections:reflections:0.10.2")

}

configurations.all {
    resolutionStrategy.cacheDynamicVersionsFor(10, "seconds")
}

tasks.withType<Jar> {
    archiveFileName.set("${rootProject.name}.jar")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
    repositories {
        val githubURL = project.findProperty("github.url") ?: ""
        val githubUsername = project.findProperty("github.auth.username") ?: ""
        val githubPassword = project.findProperty("github.auth.password") ?: ""

        val selfURL = project.findProperty("self.url") ?: ""
        val selfUsername = project.findProperty("self.auth.username") ?: ""
        val selfPassword = project.findProperty("self.auth.password") ?: ""

        maven(url = githubURL as String) {
            name = "github"
            credentials(PasswordCredentials::class) {
                username = githubUsername as String
                password = githubPassword as String
            }
        }

        maven(url = selfURL as String) {
            name = "self"
            credentials(PasswordCredentials::class) {
                username = selfUsername as String
                password = selfPassword as String
            }
        }
    }
}


tasks.register("publishGitHub") {
    dependsOn("publishMavenPublicationToGithubRepository")
    description = "Publishes to GitHub"
}

tasks.register("publishSelf") {
    dependsOn("publishMavenPublicationToSelfRepository")
    description = "Publishes to Self hosted repository"
}
