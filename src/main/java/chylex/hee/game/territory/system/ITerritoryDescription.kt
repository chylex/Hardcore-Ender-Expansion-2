package chylex.hee.game.territory.system

import chylex.hee.game.item.ItemPortalToken.TokenType.RARE
import chylex.hee.game.territory.behavior.VoidCorruptionBehavior
import chylex.hee.game.territory.system.properties.TerritoryColors
import chylex.hee.game.territory.system.properties.TerritoryDifficulty
import chylex.hee.game.territory.system.properties.TerritoryEnvironment
import chylex.hee.game.territory.system.properties.TerritoryTokenHolders
import chylex.hee.game.territory.system.storage.TerritoryEntry
import chylex.hee.game.territory.system.storage.TerritoryStorageComponent
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

interface ITerritoryDescription {
	val difficulty: TerritoryDifficulty
	
	val colors: TerritoryColors
	val environment: TerritoryEnvironment
	
	val tokenHolders: TerritoryTokenHolders
		get() = TerritoryTokenHolders.Default
	
	fun initialize(instance: TerritoryInstance, entry: TerritoryEntry, behaviors: MutableList<ITerritoryBehavior>) {
		if (entry.type == RARE) {
			behaviors.add(VoidCorruptionBehavior(entry.registerComponent(TerritoryStorageComponent.VOID_DATA)))
		}
	}
	
	fun prepareSpawnPoint(world: World, spawnPoint: BlockPos, instance: TerritoryInstance) {}
}
