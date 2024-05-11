rootProject.name = "hee"

pluginManagement {
	repositories {
		gradlePluginPortal()
		maven(url = "https://maven.neoforged.net/releases") { name = "NeoForge" }
		maven(url = rootDir.resolve("gradle/plugins/repository"))
	}
	
	plugins {
		id("hee.java") version "1"
		id("hee.kotlin") version "1"
		id("hee.library") version "1"
		id("hee.minecraft") version "1"
	}
}

for (module in listOf("core", "content", "neoforge")) {
	include(":$module")
	project(":$module").projectDir = file("./src/$module")
}
