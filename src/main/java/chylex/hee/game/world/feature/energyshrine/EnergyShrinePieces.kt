package chylex.hee.game.world.feature.energyshrine
import chylex.hee.system.Resource
import java.util.Random

object EnergyShrinePieces{
	// Loot
	
	val LOOT_GENERAL = Resource.Custom("chests/energyshrine_general")
	val LOOT_BUILDING_MATERIALS = Resource.Custom("chests/energyshrine_building_materials")
	
	fun LOOT_PICK(rand: Random) =
		if (rand.nextInt(100) < 65)
			LOOT_GENERAL
		else
			LOOT_BUILDING_MATERIALS
	
}
