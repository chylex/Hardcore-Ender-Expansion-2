package chylex.hee.debug

import chylex.hee.game.Environment
import chylex.hee.util.forge.Side
import org.apache.commons.lang3.SystemUtils
import java.io.File
import java.lang.management.ManagementFactory

internal object PowerShell {
	private fun canExecutePowershell(scriptName: String): Boolean {
		return SystemUtils.IS_OS_WINDOWS && Environment.side == Side.CLIENT && File(scriptName).exists()
	}
	
	fun setClipboardContents(file: File) {
		if (canExecutePowershell("filecopy.ps1")) {
			ProcessBuilder("powershell.exe", "-ExecutionPolicy", "Unrestricted", "-Sta", "-File", "filecopy.ps1", file.absolutePath).start()
		}
	}
	
	fun maximizeWindow() {
		if (canExecutePowershell("maximize.ps1")) {
			val pid = ManagementFactory.getRuntimeMXBean().name.split("@")[0]
			ProcessBuilder("powershell.exe", "-ExecutionPolicy", "Unrestricted", "-File", "maximize.ps1", pid).start()
		}
	}
}
