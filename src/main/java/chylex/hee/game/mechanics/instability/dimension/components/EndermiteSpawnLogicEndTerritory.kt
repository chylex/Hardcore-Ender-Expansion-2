package chylex.hee.game.mechanics.instability.dimension.components

import chylex.hee.game.entity.living.EntityMobEndermiteInstability
import chylex.hee.game.world.territory.TerritoryInstance
import net.minecraft.entity.monster.IMob
import net.minecraft.util.math.BlockPos
import net.minecraft.world.server.ServerWorld

object EndermiteSpawnLogicEndTerritory : EndermiteSpawnLogic() {
	override fun checkMobLimits(world: ServerWorld, pos: BlockPos): Boolean {
		val instance = TerritoryInstance.fromPos(pos)
		
		var hostileMobCount = 0
		var endermiteCount = 0
		
		for(entity in world.entities) {
			if (entity is IMob) {
				if (++hostileMobCount > 150) {
					return false
				}
				
				if (entity is EntityMobEndermiteInstability && instance == TerritoryInstance.fromPos(entity) && ++endermiteCount > 24) {
					return false
				}
			}
		}
		
		return true
	}
	
	override fun countExisting(world: ServerWorld, pos: BlockPos): Int {
		val instance = TerritoryInstance.fromPos(pos)
		return world.entities.filter { it is EntityMobEndermiteInstability && instance == TerritoryInstance.fromPos(it) }.count().toInt()
	}
}
