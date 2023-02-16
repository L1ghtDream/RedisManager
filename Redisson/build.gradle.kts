plugins {
    id("java")
    id("maven-publish")
}

group = "dev.lightdream"
version = getVersion("project")

repositories {
    mavenCentral()
    maven("https://repo.lightdream.dev/")
    maven("https://mvnrepository.com/artifact/redis.clients/jedis")
    maven("https://mvnrepository.com/artifact/org.jetbrains/annotations")
    maven("https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-api")
}

dependencies {
    // Project
    implementation(project(":redis-manager-common"))

    // LightDream
    implementation("dev.lightdream:logger:3.1.0")
    implementation("dev.lightdream:lambda:3.8.1")
    implementation("dev.lightdream:reflections:1.2.2")
    implementation("dev.lightdream:message-builder:3.1.2")

    // Lombok
    implementation("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.24")

    // Redisson
    implementation("org.redisson:redisson:3.17.0")

    // JetBrains
    implementation("org.jetbrains:annotations:23.1.0")

}

fun getVersion(id: String): String {
    return rootProject.extra[id] as String
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
