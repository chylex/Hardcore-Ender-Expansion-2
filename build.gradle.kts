val javaVersion: String by project

val minecraftVersion: String by project
val parchmentVersion: String by project
val kotlinForForgeVersion: String by project
val neoForgeVersion: String by project

val minecraftVersionRange: String by project
val neoForgeVersionRange: String by project
val kotlinForForgeVersionRange: String by project

plugins {
	idea
	kotlin("jvm") version "1.9.23"
	id("net.neoforged.moddev") version "1.0.10" // https://projects.neoforged.net/neoforged/ModDevGradle
}

idea {
	module {
		excludeDirs.add(file("gradle"))
		excludeDirs.add(file("run"))
		
		isDownloadSources = true
		isDownloadJavadoc = true
	}
}

repositories {
	maven(url = "https://thedarkcolour.github.io/KotlinForForge") {
		name = "KotlinForForge"
		content { includeGroup("thedarkcolour") }
	}
}

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(javaVersion))
	}
	
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

neoForge {
	version = neoForgeVersion
	
	parchment.minecraftVersion.set(minecraftVersion)
	parchment.mappingsVersion.set(parchmentVersion)
	
	runs {
		configureEach {
			gameDirectory.set(file("run"))
			systemProperty("forge.logging.console.level", "debug")
		}
		
		register("client") {
			ideName.set("Client")
			client()
		}
		
		register("server") {
			ideName.set("Server")
			server()
		}
		
		register("data") {
			ideName.set("Data")
			data()
			
			programArguments.apply {
				add("--all")
				addAll("--mod", "hee")
				addAll("--existing", file("src/main/resources").absolutePath)
				addAll("--output", file("src/generated/resources").absolutePath)
			}
		}
	}
}

dependencies {
	implementation("thedarkcolour:kotlinforforge-neoforge:$kotlinForForgeVersion")
}

tasks.withType<JavaCompile> {
	options.encoding = "UTF-8"
}

tasks.withType<AbstractArchiveTask>().configureEach {
	isPreserveFileTimestamps = false
	isReproducibleFileOrder = true
}

tasks.processResources {
	inputs.property("version", version)
	inputs.property("minecraftVersionRange", minecraftVersionRange)
	inputs.property("neoForgeVersionRange", neoForgeVersionRange)
	inputs.property("kotlinForForgeVersionRange", kotlinForForgeVersionRange)
	
	filesMatching("META-INF/neoforge.mods.toml") {
		expand(inputs.properties)
	}
}
