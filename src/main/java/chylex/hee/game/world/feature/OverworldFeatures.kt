package chylex.hee.game.world.feature
import chylex.hee.game.world.feature.stronghold.StrongholdGenerator
import net.minecraftforge.fml.common.registry.GameRegistry

object OverworldFeatures{
	fun register(){
		GameRegistry.registerWorldGenerator(StrongholdGenerator, Int.MAX_VALUE)
	}
}
