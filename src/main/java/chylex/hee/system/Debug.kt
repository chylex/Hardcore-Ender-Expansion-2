package chylex.hee.system
import chylex.hee.HEE
import chylex.hee.proxy.Environment
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.SubscribeEvent
import net.minecraft.client.renderer.BannerTextures
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.common.MinecraftForge
import org.lwjgl.LWJGLUtil
import org.lwjgl.LWJGLUtil.PLATFORM_WINDOWS
import org.lwjgl.opengl.Display
import java.io.File
import java.io.FileOutputStream
import java.lang.management.ManagementFactory
import java.util.Properties

object Debug{
	val enabled = System.getProperty("hee.debug") != null
	
	fun initialize(){
		if (enabled){
			when(Environment.side){
				Side.CLIENT -> {
					Display.setTitle("${Display.getTitle()} - Hardcore Ender Expansion ${HEE.version}")
					
					try{
						enableInfiniteBannerTextures()
					}catch(t: Throwable){
						t.printStackTrace()
					}
				}
				
				Side.SERVER -> {
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
				MinecraftForge.EVENT_BUS.register(object : Any(){
					@SubscribeEvent
					fun test(e: GuiOpenEvent){
						val pid = ManagementFactory.getRuntimeMXBean().name.split("@")[0]
						ProcessBuilder("powershell.exe", "-ExecutionPolicy", "Unrestricted", "-File", "maximize.ps1", pid).start()
						
						MinecraftForge.EVENT_BUS.unregister(this)
					}
				})
			}
		}
	}
	
	fun setClipboardContents(file: File){
		if (canExecutePowershell("filecopy.ps1")){
			ProcessBuilder("powershell.exe", "-ExecutionPolicy", "Unrestricted", "-Sta", "-File", "filecopy.ps1", file.absolutePath).start()
		}
	}
	
	private fun canExecutePowershell(scriptName: String): Boolean{
		return LWJGLUtil.getPlatform() == PLATFORM_WINDOWS && Environment.side == Side.CLIENT && File(scriptName).exists()
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
