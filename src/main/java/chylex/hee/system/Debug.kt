package chylex.hee.system
import chylex.hee.HEE
import net.minecraft.client.renderer.BannerTextures
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.relauncher.Side.CLIENT
import net.minecraftforge.fml.relauncher.Side.SERVER
import org.lwjgl.LWJGLUtil
import org.lwjgl.LWJGLUtil.PLATFORM_WINDOWS
import org.lwjgl.opengl.Display
import java.awt.GraphicsEnvironment
import java.io.File
import java.io.FileOutputStream
import java.lang.management.ManagementFactory
import java.util.Properties

object Debug{
	val enabled = System.getProperty("hee.debug") != null
	
	fun initialize(){
		if (enabled){
			when(FMLCommonHandler.instance().side!!){
				CLIENT -> {
					Display.setTitle("${Display.getTitle()} - Hardcore Ender Expansion ${HEE.version}")
					
					try{
						enableInfiniteBannerTextures()
					}catch(t: Throwable){
						t.printStackTrace()
					}
				}
				
				SERVER -> {
					try{
						FileOutputStream("eula.txt").use {
							val properties = Properties()
							properties["eula"] = "true"
							properties.store(it, "End User License Annoyance")
						}
					}catch(e: Exception){
						// ignore
					}
				}
			}
			
			if (canExecutePowershell("maximize.ps1")){
				val pid = ManagementFactory.getRuntimeMXBean().name.split("@")[0]
				ProcessBuilder("powershell.exe", "-ExecutionPolicy", "Unrestricted", "-File", "maximize.ps1", pid).start()
			}
		}
	}
	
	fun setClipboardContents(file: File){
		if (canExecutePowershell("filecopy.ps1")){
			ProcessBuilder("powershell.exe", "-ExecutionPolicy", "Unrestricted", "-Sta", "-File", "filecopy.ps1", file.absolutePath).start()
		}
	}
	
	private fun canExecutePowershell(scriptName: String): Boolean{
		return LWJGLUtil.getPlatform() == PLATFORM_WINDOWS && !GraphicsEnvironment.isHeadless() && File(scriptName).exists()
	}
	
	// Special features
	
	private fun enableInfiniteBannerTextures(){
		with(BannerTextures.BANNER_DESIGNS.javaClass.getDeclaredField("cacheMap")){
			isAccessible = true
			
			@Suppress("UNCHECKED_CAST")
			val original = get(BannerTextures.BANNER_DESIGNS) as MutableMap<Any, Any>
			
			set(BannerTextures.BANNER_DESIGNS, object : MutableMap<Any, Any> by original{
				override val size = 0
			})
		}
	}
}
