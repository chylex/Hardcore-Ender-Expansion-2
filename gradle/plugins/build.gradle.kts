plugins {
	idea
	`kotlin-dsl`
	`maven-publish`
}

group = "com.chylex.hee.gradle"
version = "1"

idea {
	module {
		excludeDirs.add(file("gradle"))
		excludeDirs.add(file("repository"))
	}
}

repositories {
	gradlePluginPortal()
	maven(url = "https://maven.neoforged.net/releases") { name = "NeoForge" }
}

dependencies {
	implementation(group = "net.neoforged.gradle", name = "userdev", version = "7.0.93")
	implementation(group = "org.jetbrains.kotlin", name = "kotlin-gradle-plugin", version = "1.9.22")
}

publishing {
	repositories {
		maven(url = projectDir.resolve("repository"))
	}
}
