package chylex.hee.game.mechanics.instability.dimension.components
import chylex.hee.game.entity.living.EntityMobEndermiteInstability
import net.minecraft.entity.monster.IMob
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

object EndermiteSpawnLogicOverworld : EndermiteSpawnLogic(){
	override fun checkMobLimits(world: World, pos: BlockPos): Boolean{
		var hostileMobCount = 0
		var endermiteCount = 0
		
		for(entity in world.loadedEntityList){
			if (entity is IMob){
				if (++hostileMobCount > 150){
					return false
				}
				
				if (entity is EntityMobEndermiteInstability && ++endermiteCount > 40){
					return false
				}
			}
		}
		
		return true
	}
	
	override fun countExisting(world: World, pos: BlockPos): Int{
		return world.loadedEntityList.count { it is EntityMobEndermiteInstability }
	}
}
