plugins {
	java
}

sourceSets {
	configureEach {
		
		java.setSrcDirs(listOf(file("./$name/java")))
		resources.setSrcDirs(listOf(file("./$name/resources")))
	}
}

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(17))
	}
}

tasks.withType<JavaCompile> {
	options.encoding = "UTF-8"
}
