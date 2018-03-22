package chylex.hee.system
import chylex.hee.HardcoreEnderExpansion
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.relauncher.Side.CLIENT
import net.minecraftforge.fml.relauncher.Side.SERVER
import org.lwjgl.opengl.Display
import java.io.FileOutputStream
import java.util.Properties

object Debug{
	val enabled = System.getProperty("hee.debug") != null
	
	fun initialize(){
		if (enabled){
			when(FMLCommonHandler.instance().side!!){
				CLIENT -> {
					Display.setTitle("${Display.getTitle()} - Hardcore Ender Expansion ${HardcoreEnderExpansion.version}")
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
		}
	}
}
