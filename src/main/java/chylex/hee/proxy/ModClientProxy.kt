package chylex.hee.proxy
import chylex.hee.client.util.MC
import chylex.hee.game.commands.HeeClientCommand
import chylex.hee.init.ModRendering
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import net.minecraftforge.client.ClientCommandHandler

@Suppress("unused", "RemoveExplicitTypeArguments")
@Sided(Side.CLIENT)
class ModClientProxy : ModCommonProxy(){
	override fun getClientSidePlayer() = MC.player
	
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
		val settings = MC.settings
		
		if (settings.particleSetting != Int.MAX_VALUE){
			prevParticleSetting = settings.particleSetting
		}
		
		settings.particleSetting = Int.MAX_VALUE
	}
	
	override fun resumeParticles(){
		if (prevParticleSetting != Int.MAX_VALUE){
			MC.settings.particleSetting = prevParticleSetting
		}
	}
}
