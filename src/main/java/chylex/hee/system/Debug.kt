package chylex.hee.system
import chylex.hee.HEE
import chylex.hee.client.util.MC
import chylex.hee.proxy.Environment
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.SubscribeEvent
import net.minecraft.client.renderer.BannerTextures
import net.minecraft.util.SharedConstants
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.client.event.InputEvent.KeyInputEvent
import net.minecraftforge.common.MinecraftForge
import org.apache.commons.lang3.SystemUtils
import org.lwjgl.glfw.GLFW
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
					MC.instance.execute {
						GLFW.glfwSetWindowTitle(MC.window.handle, "Minecraft ${SharedConstants.getVersion().name} - Hardcore Ender Expansion ${HEE.version}")
					}
					
					MinecraftForge.EVENT_BUS.register(object : Any(){
						@SubscribeEvent
						fun onKeyPressed(e: KeyInputEvent){
							if (e.action == 1 && e.key == GLFW.GLFW_KEY_GRAVE_ACCENT){
								val player = MC.player ?: return
								
								if (player.isCreative){
									player.sendChatMessage("/gamemode survival")
								}
								else{
									player.sendChatMessage("/gamemode creative")
								}
							}
						}
					})
					
					try{
						enableInfiniteBannerTextures()
					}catch(t: Throwable){
						t.printStackTrace()
					}
				}
				
				Side.DEDICATED_SERVER -> {
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
					fun onGuiOpen(@Suppress("UNUSED_PARAMETER") e: GuiOpenEvent){
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
		return SystemUtils.IS_OS_WINDOWS && Environment.side == Side.CLIENT && File(scriptName).exists()
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
