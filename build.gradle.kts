plugins {
	idea
}

idea {
	module {
		excludeDirs.add(file("gradle"))
		excludeDirs.add(file("run"))
		
		isDownloadSources = true
		isDownloadJavadoc = true
	}
}
