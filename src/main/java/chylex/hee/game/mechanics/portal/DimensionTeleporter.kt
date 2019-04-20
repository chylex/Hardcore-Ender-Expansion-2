package chylex.hee.game.mechanics.portal
import chylex.hee.system.util.center
import chylex.hee.system.util.getPosOrNull
import chylex.hee.system.util.heeTagPersistent
import chylex.hee.system.util.heeTagPersistentOrNull
import chylex.hee.system.util.motionVec
import chylex.hee.system.util.setPos
import chylex.hee.system.util.subtractY
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraftforge.common.util.ITeleporter

sealed class DimensionTeleporter{
	private companion object{
		private fun spawnPoint(world: World): Vec3d{
			return world.spawnPoint.let(world::getTopSolidOrLiquidBlock).center.subtractY(0.49)
		}
		
		private fun placeAt(entity: Entity, target: Vec3d, yaw: Float){
			entity.setLocationAndAngles(target.x, target.y, target.z, yaw, 0F)
			entity.motionVec = Vec3d.ZERO
		}
	}
	
	object LastPortal : ITeleporter{
		private const val LAST_PORTAL_POS_TAG = "LastPortal"
		
		fun updateForEntity(entity: Entity, pos: BlockPos){
			if (entity.dimension == 0){
				entity.heeTagPersistent.setPos(LAST_PORTAL_POS_TAG, pos)
			}
		}
		
		override fun placeEntity(world: World, entity: Entity, yaw: Float){
			val target = entity
				.takeIf { it.dimension == 0 }
				?.heeTagPersistentOrNull
				?.getPosOrNull(LAST_PORTAL_POS_TAG)
				?.let { it.center.subtractY(0.45) }
			
			placeAt(entity, target ?: spawnPoint(world), yaw)
		}
	}
	
	object Bed : ITeleporter{ // TODO use at the end
		override fun placeEntity(world: World, entity: Entity, yaw: Float){
			val dimension = world.provider.dimension
			
			val target = (entity as? EntityPlayer)
				?.let { entity.getBedLocation(dimension) }
				?.let { EntityPlayer.getBedSpawnLocation(world, it, entity.isSpawnForced(dimension)) }
				?.let { it.center.subtractY(0.4) }
			
			placeAt(entity, target ?: spawnPoint(world), yaw)
		}
	}
}
