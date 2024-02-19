plugins {
	id("hee.java")
}

sourceSets {
	main {
		resources {
			srcDir(rootProject.layout.projectDirectory.dir("resources/common"))
		}
	}
}
