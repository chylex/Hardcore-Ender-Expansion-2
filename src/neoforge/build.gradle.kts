val minecraftVersionRange: String by project
val neoForgeVersionRange: String by project
val kotlinForForgeVersionRange: String by project

plugins {
	id("hee.kotlin")
	id("hee.minecraft")
}

runs {
	configureEach {
		fun modSource(project: Project) {
			modSource(project.sourceSets.main.get())
		}
		
		modSource(project)
		
		workingDirectory = rootProject.layout.projectDirectory.dir("run").asFile
		systemProperty("forge.logging.console.level", "debug")
	}
	
	create("client")
	create("server")
}

tasks.processResources {
	inputs.property("version", version)
	inputs.property("minecraftVersionRange", minecraftVersionRange)
	inputs.property("neoForgeVersionRange", neoForgeVersionRange)
	inputs.property("kotlinForForgeVersionRange", kotlinForForgeVersionRange)
	
	filesMatching("META-INF/mods.toml") {
		expand(inputs.properties)
	}
}

tasks.jar {
	manifest {
		attributes(
			"Specification-Title" to "hee",
			"Specification-Vendor" to "chylex",
			"Specification-Version" to "0",
			"Implementation-Title" to "Hardcore-Ender-Expansion-2",
			"Implementation-Vendor" to "chylex",
			"Implementation-Version" to version,
		)
	}
}
