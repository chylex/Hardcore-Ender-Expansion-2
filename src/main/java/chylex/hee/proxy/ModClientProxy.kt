package chylex.hee.proxy
import chylex.hee.game.commands.HeeClientCommand
import chylex.hee.init.ModRendering
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@Suppress("unused", "RemoveExplicitTypeArguments")
@SideOnly(Side.CLIENT)
class ModClientProxy : ModCommonProxy(){
	override fun getClientSidePlayer(): EntityPlayer? = Minecraft.getMinecraft().player
	
	override fun onPreInit(){
		ModRendering.registerEntities()
	}
	
	override fun onInit(){
		ClientCommandHandler.instance.registerCommand(HeeClientCommand)
		
		ModRendering.registerLayers()
		ModRendering.registerTileEntities()
		ModRendering.registerBlockItemColors()
	}
	
	// Particles
	
	private var prevParticleSetting = Int.MAX_VALUE
	
	override fun pauseParticles(){
		val settings = Minecraft.getMinecraft().gameSettings
		
		if (settings.particleSetting != Int.MAX_VALUE){
			prevParticleSetting = settings.particleSetting
		}
		
		settings.particleSetting = Int.MAX_VALUE
	}
	
	override fun resumeParticles(){
		if (prevParticleSetting != Int.MAX_VALUE){
			Minecraft.getMinecraft().gameSettings.particleSetting = prevParticleSetting
		}
	}
}
