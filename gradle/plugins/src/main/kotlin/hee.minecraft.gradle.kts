val neoForgeVersion: String by project

plugins {
	id("hee.java")
	id("net.neoforged.gradle.userdev")
}

dependencies {
	implementation("net.neoforged:neoforge:$neoForgeVersion")
}

runs.clear()
