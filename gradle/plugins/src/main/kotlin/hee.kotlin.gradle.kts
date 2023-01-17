val kotlinForForgeVersion: String by project

plugins {
	id("hee.java")
	kotlin("jvm")
}

sourceSets {
	configureEach {
		java.setSrcDirs(emptyList<File>())
		kotlin.setSrcDirs(listOf(file("./$name/kotlin")))
	}
}

repositories {
	maven(url = "https://thedarkcolour.github.io/KotlinForForge") {
		name = "KotlinForForge"
		content { includeGroup("thedarkcolour") }
	}
}

dependencies {
	implementation("thedarkcolour:kotlinforforge-neoforge:${kotlinForForgeVersion}")
}
