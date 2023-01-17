plugins {
	id("hee.java")
}

val generateManifest = tasks.register("generateManifest") {
	val outputDir = layout.buildDirectory.dir("generateManifest")
	outputs.dir(outputDir).withPropertyName("outputDir")
	
	doLast {
		val outputDirFile = outputDir.get().asFile
		
		outputDirFile.mkdir()
		outputDirFile.resolve("MANIFEST.MF").writeText(
			"""
			Automatic-Module-Name: com.chylex.hee.${project.name}
			FMLModType: GAMELIBRARY
			
			""".trimIndent()
		)
	}
}

tasks.processResources {
	from(generateManifest) {
		into("META-INF")
	}
}
